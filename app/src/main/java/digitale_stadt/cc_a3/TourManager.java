package digitale_stadt.cc_a3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ralf Engelken on 16.05.2016.
 *
 * The TourManager manages the GPS-positions.
 * the tour acts as a queue holding the positions, new positions are added to the queue
 * depending on the transmission-type the data is then sent to the server or saved in the db
 */
public class TourManager {

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

    Context context;

    public TourManager(Context context, String deviceID)
    {
        this.context = context;

        this.queueLength = 1;

        this.deviceID = deviceID;

        this.use_filtered_values = false;
        speedTreshold_kmh = 5.0;
        distanceTreshold_m = 40.0;

        StartNewTour();
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

    public Tour ClearWayPoints()
    {
        Tour oldTour = new Tour(tour);
        tour.ClearWayPoints();

        return oldTour;
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

            double lat = location.getLatitude();
            double lon = location.getLongitude();

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
            distance_m_all += distance;
            duration_ms_all = duration;

            lastLocation = location;

            // the waypoint is added to the tour
            tour.AddWayPoint(position);
            counter++;

            // check what to do with the data
            // if WLAN is available or not neccessary, send data
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String auth_token = sharedPrefs.getString("auth_token", "");
            boolean connected = !auth_token.equals("");
            if (connected && (WiFiAvailable() || LiveUploadChecked())) {
                //check if number of wayPoints is enough to send data
                if ((tour.GetWayPoints().size() >= queueLength) || (tour.GetTourComplete())) {
                    // send/save data and clear waypoint list in tour
                    Tour t = ClearWayPoints();
                    for (Position p : t.GetWayPoints()) {
                        DBManager.getInstance().doRequest().insertPosition(p);
                    }
                    RequestManager.getInstance().doRequest().SendTourData(auth_token, t);
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

    // returns the duration since the tour started in ms
    public long GetDuration_ms()
    {
        if (use_filtered_values)
            return duration_ms_filtered;
        else
            return duration_ms_all;
    }

    // returns the distance travelled since the tour started in km
    public double GetDistance_km() {
            if (use_filtered_values)
                return distance_m_filtered / 1000f;
            else
                return distance_m_all / 1000f;
    }

    // returns the average speed in km/h
    public double GetAvgSpeed_kmh() {
        double distance_m;
        double duration_ms;

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

    public boolean SaveTourToDB() {
        try
        {
            for (Position pos : tour.GetWayPoints())
                DBManager.getInstance().doRequest().insertPosition(pos);
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
            RequestManager.getInstance().doRequest().SendTourData("", tour);
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

    // prüft ob 'LiveUpload' in den shared prefs true ist
    public boolean LiveUploadChecked(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean("wlan_upload", false);
    }
}
