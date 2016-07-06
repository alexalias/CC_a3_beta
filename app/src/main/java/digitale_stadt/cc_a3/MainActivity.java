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
import android.location.Location;
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
    TextView textInfo;
    TextView speed;
    TextView dauer;
    TextView strecke;
    TextView debug;

    boolean läuft = false;

    LocationPostCorrection locationPostCorrection = new LocationPostCorrection(10);

    //if true, the first location sent by the GPSTracker has been dropped
    int firstLocationDropped;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    // GPSTracker class
    //private GPSTracker gps;

    //Unsere GPS-Warte-Meldung
    private ProgressDialog pgGPSWait;

    // der TourManageService verwaltet alle Informationen zur Tour.
    // Er bekommt neue Positionen vom GPSTracker übergeben und sorgt für das
    //  verschicken bzw. speichern der Positionen
    //!!!Intent serviceTMIntent;

    GPSTrackerService gpsService;
    TourManagerService tmService;
    boolean mBound = false;

    private ServiceConnection gpsConnection;
    private ServiceConnection tmConnection;

    private Chronometer zeitAnzeige;

//    private DBHelper dbHelper;
//    private Sender sender;

    final String deviceID = "001";



    // ######### Lifecycle Management #######################################

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("Main", "onCreate");
        //sender = new Sender(this);
        //dbHelper = new DBHelper(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        strecke = (TextView) findViewById(R.id.streckeAnzeige);
        dauer = (TextView) findViewById(R.id.dauerAnzeige);
        speed = (TextView) findViewById(R.id.speedAnzeige);
        debug = (TextView) findViewById(R.id.debugEditTex);
        debug.setText ("");
        zeitAnzeige = (Chronometer) findViewById(R.id.dauerAnzeige);

        pgGPSWait = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);

        setTitleBackgroundColor();
        Config.mContext = this;

        RequestManager.getInstance(this);
        DBManager.getInstance(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

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

        //serviceTMIntent = new Intent(this, TourManagerService.class);

        // from TourSummaryActivity
        gpsConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i("MainActivity", "GPSTracker erfolgreich Connected, setze binder, bekomme service und setze binder = true und sei der Listener");
                GPSTrackerService.GpsBinder binder = (GPSTrackerService.GpsBinder) service;
                gpsService = binder.getService();
                mBound = true;
                // change!!
                gpsService.registerListener(MainActivity.this);
                //gpsService.registerListener(gpsService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("MainActivity", "GPSTracker Nicht erfolgreich connected setze mBound false");
                mBound = false;
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
                mBound = true;
                // change!!
                //gpsService.registerListener(MainActivity.this);
                //gpsService.registerListener(tmService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("MainActivity", "TourManagerService Nicht erfolgreich connected setze mBound false");
                mBound = false;
            }
        };

        // from TourSummaryActivity
        bindService(serviceTMIntent, tmConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        Log.i("Main", "onStart");
        super.onStart();
        //gps = new GPSTracker(this){ };

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

//        Intent intentGPS = new Intent(MainActivity.this, GPSTrackerService.class);
//        stopService(intentGPS);
//
//        Intent intentTMS = new Intent(MainActivity.this, TourManagerService.class);
//        stopService(intentTMS);
//
//        if (gps != null)
//            gps.stopUsingGPS();

        super.onDestroy();
        getDelegate().onDestroy();
    }





    // ######### öffentliche Methoden #######################################

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
            /*case R.id.action_impressum:
                return displayImpressumActivity();*/
            case R.id.action_login:
                return displayLoginActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    // Aktualisiert die Anzeige der Strecke und Geschwindigkeit
    public void UpdateView() {
        speed.setText(String.format("%.1f km/h", tmService.GetCurrentSpeed_kmh()));
        strecke.setText(String.format("%.2f km", tmService.GetDistance_km()));
    }

    // Aktualisiert die Anzeige des Benutzernamen
    public void UpdateUsername() {
        String username;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        TextView welcome = (TextView) findViewById(R.id.text_welcome);

        String token = sharedPrefs.getString("auth_token", "");
        boolean anonymous = sharedPrefs.getBoolean("anonymous", false);

        if (!token.equals("")) {
            if (anonymous == true)
                username = "Anonymous";
            else
                username = sharedPrefs.getString("username", "");

            welcome.setText("Herzlich Willkommen, " + username + "!");
        }
        else
            welcome.setText("Sie sind nicht angemeldet!");
    }

    // Zeigt die Login-Seite an
    public boolean displayLoginActivity() {
        Intent intent;

        intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return true;
    }

    // Zeigt die Registrierungs-Seite an
    public boolean displayRegisterActivity() {
        Intent intent;

        intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        return true;
    }

    // Zeigt die Settings-Seite an
    public boolean displaySettingsActivity() {
        Intent intent;
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    // Zeigt das Impressum an
    public boolean displayImpressumActivity() {
        Intent intent;
        intent = new Intent(this, ImpressumActivity.class);
        startActivity(intent);
        return true;
    }

    // Fügt den übergebenen Text in das Debug-Fenster ein
    public void UpdateDebugInfo(String string)
    {
        debug.setText(debug.getText() + " " + string);
    }

    // Loggt den DB-Zustand
    public void LogDBState(String prefix) {
        //String tourID = tourManagerService.GetTour().getTourID();
        //int tourManagerEntries = tourManagerService.GetTour().GetWayPoints().size();
        int entries = DBManager.getInstance().doRequest().selectAllPositions().size();
        int entriesNotSent = DBManager.getInstance().doRequest().selectAllPositionsNotSent().size();
        //int entriesTour = DBManager.getInstance().doRequest().selectAllPositionsFromTour(tourID).size();
        //Log.i("**********" + prefix + " System", String.format("TM: %d entries   DB: %d/%d entries sent.",
        //        tourManagerEntries, entriesNotSent, entries));
    }

    // Implementiert das Observer-Interface
    @Override
    public void update(Observable observable, Object data) {
        Log.i("MainActivity", "neue Location");

        if(läuft)
        {
            //Location loc =gpsService.getLastLocation();
            //tourManagerService.AddWayPoint((Location) data);
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

//!!!        Intent serviceTMIntent = new Intent(this, TourManagerService.class);
//        this.startService(serviceTMIntent);
//
//        // from TourSummaryActivity
//        tmConnection = new ServiceConnection() {
//            @Override
//            public void onServiceConnected(ComponentName name, IBinder service) {
//                Log.i("MainActivity", "Erfolgreich Connected, setze binder, bekomme service und setze binder = true und sei der Listener");
//                TourManagerService.tmsBinder binder = (TourManagerService.tmsBinder) service;
//                tmService = binder.getService();
//                mBound = true;
//                // change!!
//                //gpsService.registerListener(MainActivity.this);
//                gpsService.registerListener(tmService);
//            }
//
//            @Override
//            public void onServiceDisconnected(ComponentName name) {
//                Log.i("TourSummaryActivity", "Nicht erfolgreich connected setze mBound false");
//
//            }
//        };
//
//        // from TourSummaryActivity
//        bindService(serviceTMIntent, gpsConnection, Context.BIND_AUTO_CREATE);

       /* gps = new GPSTracker(MainActivity.this)
        {
            @Override
            // Überschreibt GPSTracker.onLocationChanged mit einer anonymen Methode
            // Das ist unser LocationListener
            public void onLocationChanged(Location location)
            {
                //Die neue location wird aus dem GPSTracker geholt
                getLocation();

                String s = "new Position   Lat: " + location.getLatitude() + "   Long: " + location.getLongitude();
                Log.i("Main", s);

                if (firstLocationDropped >= 0) {
                    // die neue Position wird an den Tourmanager [bergeben
                    tourManager.AddWayPoint(*//*locationPostCorrection.getSmoothenedLocation*//*(location));
                 *//*   if(pgGPSWait.isShowing())
                        pgGPSWait.dismiss();*//*
                    UpdateView();
                }
                else
                    firstLocationDropped += 1;
            }

        };*/
/*
        if(isGPSEnabled && (gps.getLocation() == null)){
            pgGPSWait.show(this, "", "Warte auf GPS Empfang");
            Log.i("PG is showing: ", pgGPSWait.isShowing()+"");
        }
*/
        UpdateView();
        // check if GPS enabled
//        if(gps.canGetLocation()){
            //get location and save it as StartLocation
//            Location location = gps.getLocation();

            // Setzt im TourManageService eine erste position
//            if (location != null) {
                //tourManagerService.AddWayPoint(location);
                //    Toast toast = Toast.makeText(getApplicationContext(), "Ihre StartPosition ist:\nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude(), Toast.LENGTH_LONG);
                //    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                //    toast.show();
//            }
//        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
//            gps.showSettingsAlert();
//        }
    }

    // Beendet die Tour. Das Tracking wird ausgeschaltet und die übrigen Daten versendet bzw. gespeichert.
    private void StopTracking() {
        Log.i("Main", "Tracking stopped");
        Toast toast = Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        läuft = false;
        //!!!this.stopService(serviceTMIntent);
        gpsService.deregisterListener();
        // Beendet die tour im TourManageService und speichert sie in die Datenbank
        //tourManagerService.StopTour();
        //tourManagerService.SaveTourToDB();
        zeitAnzeige.stop();

        Intent intentGPS = new Intent(MainActivity.this, GPSTrackerService.class);
        stopService(intentGPS);

        Intent intentTMS = new Intent(MainActivity.this, TourManagerService.class);
        stopService(intentTMS);

//        if (gps != null)
//            gps.stopUsingGPS();
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



    // ######### Event handler ##############################################

    public void TrackingButtonClicked(View view) {
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            //    Toast toast = Toast.makeText(MainActivity.this, "Tracking gestartet", Toast.LENGTH_SHORT);
            //   toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            //   toast.show();
            StartTracking();
        }
        else {
            StopTracking();
        }
    }

    public void FakeGPS(View view) {
        if (gpsService != null)
        {
            gpsService.update(null, "");
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



