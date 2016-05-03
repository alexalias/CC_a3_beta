package digitale_stadt.cc_a3;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.HashMap;

public class MainActivity extends Activity {

    Button btnShowLocation;

    // GPSTracker class
    private GPSTracker gps;
    private Sender sender;
    private Tour tour;
    private int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sender = new Sender(this);
        tour = new Tour("ralf", "TestPassword123!", 111);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gps = new GPSTracker(MainActivity.this)
                {
                    @Override
                    public void onLocationChanged(Location location)
                    {
                        getLocation();
                        String s = CreateJSONStringFromLocation(location);
                        Log.i("GPSTracker", s);
                        SendJSONString(s);
                    }
                };
//                TextView tv = (TextView) this.findViewById(R.id.hello_world);
//                //Anonymous Class
//                tv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//
//                    }
//                });


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

                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + position.getLatitude() + "\nLong: " + position.getLongitude(), Toast.LENGTH_LONG).show();
                    SendJSONString(tour.getJSONString());
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
    }

    //Specifies actions to be performed when the play/stop button is clicked
    //Our play/stop button is a toggle button!
    public void startTracking(View view){
        boolean on = ((ToggleButton) view).isChecked();
        if (on) {
            Log.i("info", "Tacking started");
            Toast.makeText(MainActivity.this, "Tacking started", Toast.LENGTH_SHORT).show();
            //trackerService = new Intent(this, DummyService.class);
            //this.startService(trackerService);
        }
        else {
            Log.i("info", "Tacking stopped");
            Toast.makeText(MainActivity.this, "Tacking stopped", Toast.LENGTH_SHORT).show();
            //this.stopService(trackerService);
        }

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
