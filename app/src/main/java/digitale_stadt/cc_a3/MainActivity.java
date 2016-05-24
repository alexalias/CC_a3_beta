package digitale_stadt.cc_a3;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

//ToDo: create loop that checks DB for new positions and sends them using the Sender
//ToDo: show map
//ToDo: get GPS signal

public class MainActivity extends AppCompatActivity {

    TextView textView;
    ArrayList<String> list;

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

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        list = new ArrayList<>();
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        tourManager = new TourManager(this, deviceID);
    }

    //Eine Sorte Clicklistener für unser start/stop Button
    public void startTracking(View view){
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            Toast.makeText(MainActivity.this, "Tracking started", Toast.LENGTH_SHORT).show();
            StartTrackingClicked();
        }
        else {
            StopTrackingClicked();
        }
    }

// GPS-Funktion wird angeschaltet und die WayPoints einer Tour im ArrayList zwischengespeichert.
    private void StartTrackingClicked()
    {
        Log.i("Main", "Tracking started");

        //Startet eine neue Tour im TourManager
        tourManager.StartNewTour();

        gps = new GPSTracker(MainActivity.this)
        {
            @Override
            // Überschreibt GPSTracker.onLocationChanged mit einer anonymen Methode
            public void onLocationChanged(Location location)
            {
                getLocation(); //is this needed?

                String s = "new Position   Lat: " + location.getLatitude() + "\nLong: " + location.getLongitude();
                Log.i("Main", s);

                // die neue Position wird an den Tourmanager [bergeben
                tourManager.AddWayPoint(location);
            }
        };

        // check if GPS enabled
        if(gps.canGetLocation()){
            //get location and save it as StartLocation
            Location location = gps.getLocation();

            // Setzt im TourManager eine erste position
            if (location != null) {
                tourManager.AddWayPoint(location);

                Toast.makeText(getApplicationContext(), "Ihre StartPosition ist:\nLat: " + location.getLatitude() + "\nLong: " + location.getLongitude(), Toast.LENGTH_LONG).show();
            }
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    //Beendet die Tour. Das Tracking wird ausgeschaltet und die übrigen Daten versendet bzw. gespeichert.
    private void StopTrackingClicked()
    {

        Log.i("Main", "Tracking stopped");
        Toast.makeText(getApplicationContext(), "Tracking wurde deaktiviert", Toast.LENGTH_SHORT).show();

        // Beendet die tour im TourManager und speichert sie in die Datenbank
        tourManager.StopTour();
        tourManager.SaveTourToDB();
        
        if (gps != null)
            gps.stopUsingGPS();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
