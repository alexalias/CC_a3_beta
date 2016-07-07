package digitale_stadt.cc_a3;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer {

    // ######### Variable ###################################################
    TextView speed;
    TextView strecke;

    boolean läuft = false;

    //if true, the first location sent by the GPSTrackerService has been dropped
    int firstLocationDropped;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    //Unsere GPS-Warte-Meldung
    private ProgressDialog pgGPSWait;

    // der TourManageService verwaltet alle Informationen zur Tour.
    // Er bekommt neue Positionen vom GPSTrackerService übergeben und sorgt für das
    //  verschicken bzw. speichern der Positionen

    GPSTrackerService gpsService;
    TourManagerService tmService;
    boolean mGPSBound = false;
    boolean mTMBound = false;

    private ServiceConnection gpsConnection;
    private ServiceConnection tmConnection;

    private Chronometer zeitAnzeige;

    final String deviceID = "001";

    // ######### Lifecycle Management #######################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Main", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        strecke = (TextView) findViewById(R.id.streckeAnzeige);
        speed = (TextView) findViewById(R.id.speedAnzeige);
        zeitAnzeige = (Chronometer) findViewById(R.id.dauerAnzeige);

        pgGPSWait = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);

        setTitleBackgroundColor();
        Config.mContext = this;

        RequestManager.getInstance(this);
        DBManager.getInstance(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        StartServices();
    }

    @Override
    public void onStart() {
        Log.i("Main", "onStart");
        super.onStart();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        RequestManager.getInstance().doRequest().Login(
                false,
                sharedPrefs.getString("username", ""),
                sharedPrefs.getString("userpassword", ""));

        if (tmService != null)
            tmService.registerListener(MainActivity.this);
    }

    @Override
    protected void onPause() {
        Log.i("Main", "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i("Main", "onResume");
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.i("Main", "onStop");
        if (tmService != null)
            tmService.deregisterListener();

        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i("Main", "onDestroy");

        StopServices();

        super.onDestroy();
        getDelegate().onDestroy();
    }

    // Menüleiste
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Menüleiste
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return displaySettingsActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // ######### öffentliche Methoden #######################################

    // Aktualisiert die Anzeige der Strecke und Geschwindigkeit
    public void UpdateView() {
        speed.setText(String.format("%.1f km/h", tmService.GetCurrentSpeed_kmh()));
        strecke.setText(String.format("%.2f km", tmService.GetDistance_km()));
    }

    // Zeigt die Settings-Seite an
    public boolean displaySettingsActivity() {
        Intent intent;
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    // Loggt den DB-Zustand
    public void LogDBState(String prefix) {
        int entries = DBManager.getInstance().doRequest().selectAllPositions().size();
        int entriesNotSent = DBManager.getInstance().doRequest().selectAllPositionsNotSent().size();
    }

    // Implementiert das Observer-Interface
    @Override
    public void update(Observable observable, Object data) {
        Log.i("MainActivity", "neue Location");

        if(läuft)
        {
            UpdateView();
        }
    }

    // ######### private Methoden ###########################################

    // GPS-Funktion wird angeschaltet und die WayPoints einer Tour im ArrayList zwischengespeichert.
    private void StartTracking() {
        Log.i("Main", "Tracking gestarted");

        //Startet eine neue Tour im TourManageService
        //tourManagerService.StartNewTour();
        zeitAnzeige.setBase(SystemClock.elapsedRealtime());
        zeitAnzeige.start();

        firstLocationDropped = -1;

        läuft = true;

        tmService.registerListener(MainActivity.this);
        tmService.StartNewTour();
        gpsService.registerListener(tmService);

        UpdateView();
    }

    // Beendet die Tour. Das Tracking wird ausgeschaltet und die übrigen Daten versendet bzw. gespeichert.
    private void StopTracking() {
        Log.i("Main", "Tracking stopped");
        Toast toast = Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        läuft = false;
        gpsService.deregisterListener();
        // Beendet die tour im TourManageService und speichert sie in die Datenbank
        zeitAnzeige.stop();
    }

    // Ändert die Hintergrundfarbe der Titelleiste
    private void setTitleBackgroundColor() {
        View titleView = getWindow().findViewById(android.R.id.title);
        if (titleView != null) {
            ViewParent parent = titleView.getParent();
            if (parent != null && (parent instanceof View)) {
                View parentView = (View)parent;
                parentView.setBackgroundColor(Color.rgb(255,0,0));
            }
        }
    }

    // Startet den GPS-Service und den TourManager-Service
    private void StartServices()
    {
        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled) {
            showSettingsAlert();
        }

        //from StartActivity
        Intent service = new Intent(this, GPSTrackerService.class);
        this.startService(service);

        // from TourSummaryActivity
        gpsConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                GPSTrackerService.GpsBinder binder = (GPSTrackerService.GpsBinder) service;
                gpsService = binder.getService();
                mGPSBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mGPSBound = false;
            }
        };

        // from TourSummaryActivity
        bindService(service, gpsConnection, Context.BIND_AUTO_CREATE);

        Intent serviceTMIntent = new Intent(this, TourManagerService.class);
        this.startService(serviceTMIntent);

        // from TourSummaryActivity
        tmConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i("MainActivity", "TourManagerService Erfolgreich Connected, setze binder, bekomme service und setze binder = true und sei der Listener");
                TourManagerService.tmsBinder binder = (TourManagerService.tmsBinder) service;
                tmService = binder.getService();
                tmService.SetContext(MainActivity.this);
                mTMBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("MainActivity", "TourManagerService Nicht erfolgreich connected setze mBound false");
                mTMBound = false;
            }
        };

        // from TourSummaryActivity
        bindService(serviceTMIntent, tmConnection, Context.BIND_AUTO_CREATE);
    }

    // Beendet den GPS-Service und den TourManager-Service
    private void StopServices()
    {
        if (tmService != null)
            tmService.deregisterListener();

        unbindService(gpsConnection);
        Intent intentGPS = new Intent(MainActivity.this, GPSTrackerService.class);
        stopService(intentGPS);

        unbindService(tmConnection);
        Intent intentTMS = new Intent(MainActivity.this, TourManagerService.class);
        stopService(intentTMS);
    }

    // ######### Event handler ##############################################

    public void TrackingButtonClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            StartTracking();
        }
        else {
            StopTracking();
        }
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS muss aktiviert sein");

        // Setting Dialog Message
        alertDialog.setMessage("GPS ist nicht aktiviert. Möchten Sie die Einstellungen öffnen?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Einstellungen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}