package digitale_stadt.cc_a3;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

//ToDo: create loop that checks DB for new positions and sends them using the Sender
//ToDo: show map
//ToDo: get GPS signal
public class MainActivity extends Activity {

    Button btnStartTracking;
    Button btnStopTracking;
    Button btnUpdate;
    Button btnSend;
    RadioButton radioButton;
    TextView textView;

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

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        // show location button click event
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateClicked();
            }
        });

        btnSend = (Button) findViewById(R.id.btnSend);
        // show location button click event
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendClicked();
            }
        });

        textView = (TextView) findViewById(R.id.textView);
        radioButton = (RadioButton) findViewById(R.id.radioButton);
    }

    private void SendClicked()
    {
        Log.i("Main", "Send");
        if (gps != null)
            dbHelper.insertPosition(new Position(tour, gps.getLocation()));
    }

    private void UpdateClicked()
    {
        Log.i("Main", "Update");
        if (gps != null)
        {
            String text = Integer.toString(dbHelper.selectAllPositionsNotSent().size());
            textView.setText(text);
        }
    }

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
                SendJSONString(s);

                dbHelper.insertPosition(new Position(tour, location));
                //ToDo: save location to DB instead of sending
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
        gps.stopUsingGPS();
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
