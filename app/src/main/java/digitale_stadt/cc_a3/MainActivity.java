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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

//ToDo: create loop that checks DB for new positions and sends them using the Sender
//ToDo: show map
//ToDo: get GPS signal
public class MainActivity extends Activity {

    Button btnStartTracking;
    Button btnStopTracking;
    Button btnShow;
    Button btnInsert;
    RadioButton radioButton;
    TextView textView;
    ListView listView;
    ArrayList<String> list;
    Timer sendTimer;

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
  //      String password = "";
        tour = new Tour();
        dbHelper = new DBHelper(this);

        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);
        // show location button click event
        btnStartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartTrackingClicked();
            }
        });

        btnStopTracking = (Button) findViewById(R.id.btnStopTracking);
        // show location button click event
        btnStopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopTrackingClicked();
            }
        });

        btnShow = (Button) findViewById(R.id.btnShow);
        // show location button click event
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowClicked();
            }
        });

//        btnInsert = (Button) findViewById(R.id.btnInsert);
//        // show location button click event
//        btnInsert.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                InsertClicked();
//            }
//        });

        textView = (TextView) findViewById(R.id.textView);
        radioButton = (RadioButton) findViewById(R.id.radioButton);
        listView = (ListView) findViewById(R.id.listView);

        list = new ArrayList<>();
        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(itemsAdapter);

        isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        //setup a timer to frequently send new DB-entries to server
        StartTimer(30000);

    }

//    private void InsertClicked()
//    {
//        Log.i("Main", "Insert");
//        Location location;
//        location = new Location("");
//        location.setLatitude(10.11);
//        location.setLongitude(50.3);
//        location.setAltitude(5.5);
//        location.setTime(123456);
//
//        Toast.makeText(this, "Neue Position: " + location.getLatitude() + "/" + location.getLongitude(), Toast.LENGTH_SHORT).show();
//        dbHelper.insertPositionAsync(new Position(tour, location), new DBHelper.DatabaseHandler<Void>() {
//            @Override
//            public void onComplete(boolean success, Void result) {
//            }
//        });
//    }

    protected void OnStop()
    {
        StopTimer();
    }

    protected void StartTimer(int interval_ms)
    {
        if (sendTimer == null)
        {
            Log.i("Main", "Timer initialized");
            sendTimer = new Timer("SendTimer", true);
        }

        Log.i("Main", "Timer started");
        //schedule sending new GPS entries in DB every 30 seconds
        sendTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //ToDo: change db query to selectAllPositionsNotSent
                ArrayList<Position> result = dbHelper.selectAllPositionsFromTour(tour.getTourID());
                list.clear();

                if ((result != null) && (result.size() > 0)) {
                    Log.i("Main", "Sending " + result.size() + " Positions");
                    //String text = "DB-Entries: " + Integer.toString(result.size());
                    //textView.setText(text);

                    String s;
                    for (int i = 0; i < Math.min(result.size(), 10); i++)
                    {
                        s = "ID: " + result.get(i).getId() + "   Pos: " + result.get(i).getLatitude() + "/" + result.get(i).getLongitude() + "\nTS: " + result.get(i).getTime();
                        list.add(s);
                    }

                    //ToDo: enable sending
                    //sender.SendTourData(new Tour());

                    //ToDo: update db if positions were sent
                }
                else
                {
                    Log.i("Main", "Nothing to do");
                }
            }
        }, 0, interval_ms);
    }

    protected void StopTimer()
    {
        Log.i("Main", "Timer stopped");
        sendTimer.cancel();
    }

    private void ShowClicked()
    {
        Log.i("Main", "Update");
        Toast.makeText(getApplicationContext(), "DB wird ausgelesen", Toast.LENGTH_SHORT).show();

       dbHelper.selectAllPositionsFromTourAsync(tour.getTourID(), new DBHelper.DatabaseHandler<ArrayList<Position>>() {
           @Override
           public void onComplete(boolean success, ArrayList<Position> result) {
               if (success) {
                   list.clear();
                   if (result != null) {
                       String text = "DB-Entries: " + Integer.toString(result.size());
                       textView.setText(text);

                       String s;
                       for (Position pos : result) {
                           s = "ID: " + pos.getId() + "   Pos: " + pos.getLatitude() + "/" + pos.getLongitude() + "\nTS: " + pos.getTime();
                           list.add(s);
                       }
                       Log.i("DB Eintrag:", list.toString());
                   }
               }
               else
               {
                   Log.i("Main", "Error");
               }
           }
        });
    }

    //Enables GPS Tracking and Saving of new Positions in the DB
    private void StartTrackingClicked()
    {
        Log.i("Main", "Tracking started");
        radioButton.setChecked(true);
        tour = new Tour();
        gps = new GPSTracker(MainActivity.this)
        {
            @Override
            public void onLocationChanged(Location location)
            {
                getLocation(); //is this needed?
                Position pos = new Position(tour, location);

                Log.i("GPSTracker", "New Location: " + pos.getJSONObject().toString());
                //Toast.makeText(this, "Neue Position: " + location.getLatitude() + "/" + location.getLongitude(), Toast.LENGTH_SHORT).show();

                dbHelper.insertPositionAsync(pos, new DBHelper.DatabaseHandler<Void>()
                {
                    @Override
                    public void onComplete(boolean success, Void result) {
                    }
                });
            }
        };

        // check if GPS enabled
        if(gps.canGetLocation()){
            gps.getLocation();
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
        radioButton.setChecked(false);
        if (gps != null)
            gps.stopUsingGPS();
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
