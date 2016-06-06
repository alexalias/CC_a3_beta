package digitale_stadt.cc_a3;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Ralf Engelken on 16.05.2016.
 *
 * The TourManager manages the GPS-positions.
 * the tour acts as a queue holding the positions, new positions are added to the queue
 * depending on the transmission-type the data is then sent to the server or saved in the db
 */
public class TourManager implements SensorEventListener
{
    Tour tour;                  //stores the tour data
    int queueLength;            //number of waypoints in the tour before data is sent
    int counter = 0;            //waypoint-id of last entry
    String deviceID;            //
    Location startLocation;     //the first position of the tour; cached for ?
    Location lastLocation;      //the last position of the tour; cached for ?
    long startTime;

    //the following values define tresholds to filter out data when user is not moving
    //filtered data is not used for distance/duration calculation, the data is always sent to server
    boolean use_filtered_values;//true: intervals with a distance < speedTreshold_kmh will be ignored
    double speedTreshold_kmh;   //the minimum speed to travel at for data to be used if use_filtered_values is set
    double distanceTreshold_m;  //the minimum distance to travel per interval for data to be used if use_filtered_values is set

    long duration_ms_all;       //the time travelled so far
    double distance_m_all;      //the distance travelled so far
    long duration_ms_filtered;  //the time travelled so far, without stops
    double distance_m_filtered; //the distance travelled so far, without stops

    Sender sender;              //the object handling the server interaction
    DBHelper dbHelper;          //the object handling the db interaction
    Context context;

    // Für die Bewegungserkennung benötigt
    boolean isMoving = false;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private long lastUpdate = 0;

    //lateral movement
    private float last_x;
    // vertical movement
    private float last_y;
    // z-axis
    private float last_z;

    private static final int SHAKE_THRESHOLD = 700;

    public TourManager(Context context, String deviceID)
    {
        this.context = context;

        sender = new Sender(context);
        dbHelper = new DBHelper(context);

        this.queueLength = 1;
        this.deviceID = deviceID;

        this.use_filtered_values = false;
        speedTreshold_kmh = 5.0;
        distanceTreshold_m = 40.0;

        //Sensor initialisieren
        senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // creates a new tour
    public void StartNewTour() {
        tour = new Tour(deviceID);
        counter = 0;

        duration_ms_filtered = 0L;
        duration_ms_all = 0L;
        distance_m_filtered = 0;
        distance_m_all = 0;
    }

    // sets the id of the last wayPoint to -1
    public void StopTour() {
        if (tour != null)
        {
            Position pos = tour.GetLastPosition();
            if (pos != null)
                pos.setId(-1);
        }
    }

    public void ClearWayPoints()
    {
        tour.ClearWayPoints();
    }

    // manages the tour data
    // there are 3 cases:
    // 1. sendDirect = true; queue not full
    //     the new position will be added to the tour.
    // 2. sendDirect = true; queue is full
    //     the new position will be added to the tour. afterwards, all positions in the tour will be sent to the server.
    //     if the transmission us successfull, the positions in the tour are deleted.
    // 3. sendDirect = false
    //    the new position is added to the tour
    public void AddWayPoint(Location location) {
        if ((tour != null) && (location != null)) {
            Position position = new Position(tour.getTourID(), counter, location);

            if (counter == 0) {
                startLocation = location;
                lastLocation = location;
                startTime = startLocation.getTime();
            }

            //calculate distance to last waypoint
            float distance = location.distanceTo(lastLocation);
            long duration = location.getTime() - startTime;

            Log.i("Movement", String.format("dist: %.3f   bearing: %.3f", distance, lastLocation.bearingTo(location)));
            //update filtered data if distance is higher than treshold
            if ((distance > distanceTreshold_m) || (distance * 3600 / (double)duration > speedTreshold_kmh))
            {
                distance_m_filtered += distance;
                duration_ms_filtered += duration;
            }

            //update all data
            distance_m_all += lastLocation.getTime() - location.getTime();

            duration_ms_all = duration;

            lastLocation = location;

            // the waypoint is added to the tour
            tour.AddWayPoint(position);
            counter++;

            // check what to do with the data
            // if WLAN is available or not neccessary, send data
            if ((WiFiAvailable()) || (!WlanUploadChecked())) {
                //check if number of wayPoints is enough to send data
                if ((tour.GetWayPoints().size() >= queueLength) || (tour.GetTourComplete())) {
                    // send data and clear waypoint list in tour
                    if (SendTourToServer())
                        tour.ClearWayPoints();
                }
            }
        }
    }

    public void SetQueueLength(int queueLength) {
        this.queueLength = queueLength;
    }

    public int GetQueueLength() {
        return queueLength;
    }

    public Tour GetTour() {
        return tour;
    }

    public Location GetStartLocation() {
        return startLocation;
    }

    public Location GetLastLocation() {
        return lastLocation;
    }

    public String GetStartTime(){
        Date t = new Date(startTime);
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String time= df.format(t);
        return time;
    }

    // returns the duration since the tour started in ms
    public String GetDuration_ms()
    {
        Date t0 = new Date(duration_ms_filtered - TimeZone.getDefault().getDSTSavings());
        Date t1 = new Date(duration_ms_all - TimeZone.getDefault().getDSTSavings());
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        String d_filtered= df.format(t0);
        String d_all = df.format(t1);

        if (use_filtered_values)
            return d_filtered;
        else
            return d_all;
    }

    // returns the distance travelled since the tour started in km
    public double GetDistance_km() {

        if (isMoving) {
            if (use_filtered_values)
                return distance_m_filtered / 1000f;
            else
                return distance_m_all / 1000f;
        }
            else return 0f;
    }

    // returns the average speed in km/h
    public double GetAvgSpeed_kmh() {
        double distance_m;
        double duration_ms;

        if (isMoving) {
        if (use_filtered_values) {
            distance_m = distance_m_filtered;
            duration_ms = duration_ms_filtered;
        } else {
            distance_m = distance_m_all;
            duration_ms = duration_ms_all;
        }

        if (duration_ms != 0)
            return (distance_m * 3600) / ((double) duration_ms);
        else
            return 0;
        }
        else return 0f;
    }

    public boolean LoadTourDataFromDB(String tourID) {
        return false;
    }

    public boolean SaveTourToDB() {
        try
        {
            for (Position pos : tour.GetWayPoints())
                dbHelper.insertPosition(pos);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean SendTourToServer() {
        try
        {
            sender.SendTourData(tour);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public boolean WiFiAvailable (){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        return isWiFi;

    }

    // prüft ob 'datenupload nur bei WLAN' in den shared prefs true ist
    public boolean WlanUploadChecked(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("wlan_upload", false);
    }

    // Hier soll geprüft werden, ob sich das Handy überhaupt bewegt

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            // Beschleunigung in m/s^2
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    isMoving = true;
                }
                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
