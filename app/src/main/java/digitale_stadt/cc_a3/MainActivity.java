package digitale_stadt.cc_a3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity{

    TextView textInfo;
    TextView speed;
    TextView dauer;
    TextView strecke;
    TextView debug;

    LocationPostCorrection locationPostCorrection = new LocationPostCorrection(10);

    //if true, the first location sent by the GPSTracker has been dropped
    boolean firstLocationDropped;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    // GPSTracker class
    private GPSTracker gps;

    // der TourManager verwaltet alle Informationen zur Tour.
    // Er bekommt neue Positionen vom GPSTracker übergeben und sorgt für das
    //  verschicken bzw. speichern der Positionen
    private TourManager tourManager;

//    private DBHelper dbHelper;
//    private Sender sender;

    final String deviceID = "001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //sender = new Sender(this);
        //dbHelper = new DBHelper(this);

        RequestManager.getInstance(this);
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInfo = (TextView) findViewById(R.id.textInfo);

        strecke = (TextView) findViewById(R.id.streckeAnzeige);
        dauer = (TextView) findViewById(R.id.dauerAnzeige);
        speed = (TextView) findViewById(R.id.speedAnzeige);
        debug = (TextView) findViewById(R.id.debugEditTex);
        debug.setText ("");

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        tourManager = new TourManager(this, deviceID);

        firstLocationDropped = false;

        setTitleBackgroundColor();
    }

    //Eine Sorte Clicklistener für unser start/stop Button
    public void startTracking(View view){
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            Toast toast = Toast.makeText(MainActivity.this, "Tracking gestartet", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            StartTrackingClicked();
        }
        else {
            StopTrackingClicked();
        }
    }

// GPS-Funktion wird angeschaltet und die WayPoints einer Tour im ArrayList zwischengespeichert.
    private void StartTrackingClicked()
    {
        Log.i("Main", "Tracking gestarted");

        //Startet eine neue Tour im TourManager
        tourManager.StartNewTour();

        firstLocationDropped = false;

        gps = new GPSTracker(MainActivity.this)
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

                if (firstLocationDropped) {
                    // die neue Position wird an den Tourmanager [bergeben
                    tourManager.AddWayPoint(locationPostCorrection.getSmoothenedLocation(location));
                    UpdateView();
                }
                else
                    firstLocationDropped = true;
            }

        };

        UpdateView();
        // check if GPS enabled
        if(gps.canGetLocation()){
            //get location and save it as StartLocation
            Location location = gps.getLocation();

            // Setzt im TourManager eine erste position
            if (location != null) {
                tourManager.AddWayPoint(location);
                Toast toast = Toast.makeText(getApplicationContext(), "Ihre StartPosition ist:\nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
                toast.show();
            }
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    public void UpdateView() {
        speed.setText(String.format("%.3f km/h", tourManager.GetAvgSpeed_kmh()));

        Date t1 = new Date(tourManager.GetDuration_ms() - TimeZone.getDefault().getDSTSavings());
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String s = df.format(t1);
        dauer.setText(String.format("%s h", s));

        strecke.setText(String.format("%.3f km", tourManager.GetDistance_km()));
    }

    public void UpdateDebugInfo(String string)
    {
        debug.setText(debug.getText() + " " + string);
    }

    //Beendet die Tour. Das Tracking wird ausgeschaltet und die übrigen Daten versendet bzw. gespeichert.
    private void StopTrackingClicked()
    {

        Log.i("Main", "Tracking stopped");
        Toast toast = Toast.makeText(getApplicationContext(), "Tracking gestoppt", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        // Beendet die tour im TourManager und speichert sie in die Datenbank
        tourManager.StopTour();
        tourManager.SaveTourToDB();

        
        if (gps != null)
            gps.stopUsingGPS();
    }

    public void UpdateUsername()
    {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                return displaySettingsActivity();
            case R.id.action_impressum:
                return displayImpressumActivity();
            case R.id.action_login:
                return displayLoginActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean displayLoginActivity()
    {
        Intent intent;

        intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        return true;
    }

    public boolean displayRegisterActivity()
    {
        Intent intent;

        intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        return true;
    }

    public boolean displaySettingsActivity()
    {
        Intent intent;
        intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

    public boolean displayImpressumActivity()
    {
        Intent intent;
        intent = new Intent(this, ImpressumActivity.class);
        startActivity(intent);
        return true;
    }

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
}



