package digitale_stadt.cc_a3;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
                gps = new GPSTracker(MainActivity.this);

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
                    String url = "https://preview.api.cycleyourcity.jmakro.de:4040/log_coordinates.php";
                    HashMap<String,String> map = new HashMap<>();
                    map.put("data", tour.getJSONString() );

                    Log.i("JSON", tour.getJSONString());
                    sender.SendPostRequest(url, map);
                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
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
