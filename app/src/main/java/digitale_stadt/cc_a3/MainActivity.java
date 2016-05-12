package digitale_stadt.cc_a3;

import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;

//ToDo: create loop that checks DB for new positions and sends them using the Sender
//ToDo: show map
//ToDo: get GPS signal
public class MainActivity extends Activity {

    TextView textView;
    ArrayList<String> list;

    // Liste aller Positionen einer Tour, zum Zwischenspeichern bis Senden.
    ArrayList<Position> wayPointList;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    protected LocationManager locationManager;

    // GPSTracker class
    private GPSTracker gps;
    private DBHelper dbHelper;
    private Sender sender; //ToDo: replace with DBReader-Service
    private Tour tour;
    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sender = new Sender(this);
  //      String user = "";
  //     String password = "";
        tour = new Tour();
        dbHelper = new DBHelper(this);

        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);

        list = new ArrayList<>();
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        wayPointList = new ArrayList<>();

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //Eine Sorte Clicklistener für unser start/stop Button
    public void startTracking(View view){
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            Toast.makeText(MainActivity.this, "Tacking started", Toast.LENGTH_SHORT).show();
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
        tour = new Tour();
        gps = new GPSTracker(MainActivity.this)
        {
            @Override
            public void onLocationChanged(Location location)
            {
                getLocation(); //is this needed?
                //Create and send JSON-String
                String s = CreateJSONStringFromLocation(location);
                Log.i("GPSTracker", s);
                //SendJSONString(s);

//                Toast.makeText(this, "Neue Position: " + location.getLatitude() + "/" + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                dbHelper.insertPositionAsync(new Position(tour, location), new DBHelper.DatabaseHandler<Void>() {
//                    @Override
//                    public void onComplete(boolean success, Void result) {
//
//                    }
//                });
                wayPointList.add(new Position(tour, location));
            }
        };

        // check if GPS enabled
        if(gps.canGetLocation()){
            Position position = new Position();
            position.setLatitude(gps.getLatitude());
            position.setLongitude(gps.getLongitude());
            position.setAltitude(gps.getAltitude());
            position.setId(id);
            id += 1;

            tour.getWayPoints().clear();
            tour.AddWayPoint(position);

            Toast.makeText(getApplicationContext(), "Ihre Position ist - \nLat: " + position.getLatitude() + "\nLong: " + position.getLongitude(), Toast.LENGTH_LONG).show();
            //SendJSONString(tour.getJSONString());
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void StopTrackingClicked()
    {
        Log.i("Main", "Tracking stopped");
        Toast.makeText(getApplicationContext(), "Tracking wird deaktiviert", Toast.LENGTH_SHORT).show();
        
        SaveTourInDB();
        
        if (gps != null)
            gps.stopUsingGPS();
    }

    private void SaveTourInDB()
    {
        Log.i("Main", "Update");
        Toast.makeText(getApplicationContext(), "DB wird befüllt", Toast.LENGTH_SHORT).show();

        //ToDo: Insert Tour in DB aus dem ArrayList, Async

//        dbHelper.selectAllPositionsFromTourAsync(tour.getTourID(), new DBHelper.DatabaseHandler<ArrayList<Position>>() {
//            @Override
//            public void onComplete(boolean success, ArrayList<Position> result) {
//                if (success) {
//                    list.clear();
//                    if (result != null) {
//                        String text = "DB-Entries: " + Integer.toString(result.size());
//                        textView.setText(text);
//
//                        String s;
//                        for (Position pos : result) {
//                            s = "ID: " + pos.getId() + "   Pos: " + pos.getLatitude() + "/" + pos.getLongitude() + "\nTS: " + pos.getTime();
//                            list.add(s);
//                        }
//                        Log.d("DB Eintrag:", list.toString());
//                    }
//                }
//            }
//        });

    }

    private void SendJSONString (String s)
    {
        String url = "https://preview.api.cycleyourcity.jmakro.de:4040/log_coordinates.php";
        HashMap<String,String> map = new HashMap<>();
        map.put("data", s );

        Log.i("MAIN", "Sending " + s);
        sender.SendPostRequest(url, map);
    }

    private String CreateJSONStringFromLocation(Location loc)
    {
        Position position = new Position();
        position.setLatitude(loc.getLatitude());
        position.setLongitude(loc.getLongitude());
        position.setAltitude(loc.getAltitude());
        //Log.i("time",loc.getTime().);
        position.setId(id);
        id += 1;

        tour.getWayPoints().clear();
        tour.AddWayPoint(position);

        return tour.getJSONString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
