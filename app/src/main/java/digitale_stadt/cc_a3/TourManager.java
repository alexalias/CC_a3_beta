package digitale_stadt.cc_a3;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

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
public class TourManager implements ITourManager
{
    Tour tour;                  //stores the tour data
    int queueLength;            //number of waypoints in the tour before data is sent
    double distanceTreshold;    //the minimum distance to travel per interval for data to be used if ignoreStops is set
    boolean ignoreStops;        //true: intervals with a distance < distanceTreshold will be ignored
    int counter = 0;            //waypoint-id of last entry
    String deviceID;            //
    Location startLocation;     //the first position of the tour; cached for ?
    Location lastLocation;      //the last position of the tour; cached for ?
    long startTime;
    long duration_ms_all;       //the time travelled so far
    long duration_ms_filtered;  //the time travelled so far
    double distance_m_all;      //the distance travelled so far
    double distance_m_filtered; //the distance travelled so far
    //Sender sender;              //the object handling the server interaction
    //DBHelper dbHelper;          //the object handling the db interaction
    Context context;

    public TourManager(Context context, String deviceID)
    {
        this.context = context;

        //sender = new Sender(context);
        //dbHelper = new DBHelper(context);

        this.queueLength = 3;
        this.deviceID = deviceID;

        StartNewTour();
    }

    // creates a new tour
    public void StartNewTour() {
        if (tour == null || tour.GetWayPoints().size() == 0) {
            tour = new Tour(deviceID);
            counter = 0;
        }
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
            }

            //calculate distance to last waypoint
            float distance = location.distanceTo(lastLocation);
            //update filtered data if distance is higher than treshold
            if (distance > 0.05)
            {
                distance_m_filtered += distance;
                duration_ms_filtered += location.getTime() - lastLocation.getTime();
            }
            //update all data
            distance_m_all += distance;
            duration_ms_all += location.getTime() - lastLocation.getTime();

            lastLocation = location;
            startTime = startLocation.getTime();

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

    public long GetDuration_ms()
    {
        if (ignoreStops)
            return duration_ms_filtered;
        else
            return duration_ms_all;
    }

    public double GetDistance_km() {
        if (ignoreStops)
            return distance_m_filtered / 1000;
        else
            return distance_m_all / 1000;
    }

    public double GetAvgSpeed_kmh() {
        if (ignoreStops)
            return distance_m_filtered / ((double)duration_ms_filtered)/3600000;
        else
            return distance_m_all / duration_ms_all;
    }

    public boolean LoadTourDataFromDB(String tourID) {
        return false;
    }

    public boolean SaveTourToDB() {
        try
        {
            //for (Position pos : tour.GetWayPoints())
            //    dbHelper.insertPosition(pos);

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
            //sender.SendTourData(tour);
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
}
