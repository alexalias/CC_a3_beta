package digitale_stadt.cc_a3;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by Ralf Engelken on 16.05.2016.
 *
 * The TourManagerService manages the GPS-positions.
 * the tour acts as a queue holding the positions, new positions are added to the queue
 * depending on the transmission-type the data is then sent to the server or saved in the db
 */
public class TourManagerService extends Service implements Observer {

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
    double speed;               //actual speed

    Context context;

    Observer listener;          //Observable pattern

    private final IBinder mBinder = new tmsBinder();

    public TourManagerService(){}

    public TourManagerService(Context context, String deviceID)
    {
        this.context = context;

        this.queueLength = 1;

        this.deviceID = deviceID;

        this.use_filtered_values = false;
        speedTreshold_kmh = 5.0;
        distanceTreshold_m = 40.0;

        StartNewTour();
    }

    public class tmsBinder extends Binder {

        TourManagerService getService() {
            return TourManagerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid) {

        this.queueLength = 1;
        this.use_filtered_values = false;
        speedTreshold_kmh = 5.0;
        distanceTreshold_m = 40.0;

        Log.i("TourManagerService", "onStartCommand");
        StartNewTour();
        return 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.i("TourManagerService", "onBind, erstelle Object und sei der Observer");
        //Brauchen wir hier kein GPSTrackerService aufzurufen?
        return mBinder;
    }



    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("TourManagerService", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy(){
        //Kann sein, dass wir es nicht brauchen...
        super.onDestroy();
    }

    public void SetContext(Context context)
    {
        this.context = context;
    }

    // creates a new tour
    public void StartNewTour() {
        tour = new Tour(deviceID);
        counter = 0;

        duration_ms_filtered = 0L;
        duration_ms_all = 0L;
        distance_m_filtered = 0;
        distance_m_all = 0;
        speed = 0.0;
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
        Log.i("TourManagerService", "AddWayPoint");
        if ((tour != null) && (location != null)) {
            Log.i("TourManagerService", "location ok");
            Position position = new Position(tour.getTourID(), counter, location);

            if (counter == 0) {
                startLocation = location;
                lastLocation = location;
                startTime = startLocation.getTime();
            }

            double lat = location.getLatitude();
            double lon = location.getLongitude();
            speed = (location.getSpeed() * 3600)/1000;

            //calculate distance to last waypoint
            float distance = location.distanceTo(lastLocation);
            long duration = location.getTime() - startTime;

            //Log.i("Movement", String.format("dist: %.3f   bearing: %.3f", distance, lastLocation.bearingTo(location)));
            //update filtered data if distance is higher than treshold
            if ((distance > distanceTreshold_m) || (distance * 3600 / (double)duration > speedTreshold_kmh))
            {
                distance_m_filtered += distance;
                duration_ms_filtered += duration;
            }

            //update all data
            distance_m_all += distance;
            duration_ms_all = duration;
            //Log.i("Distance: ", distance_m_all + "");

            lastLocation = location;

            // the waypoint is added to the tour and the DB
            DBManager.getInstance().doRequest().insertPosition(position);
            tour.AddWayPoint(position);
            counter++;

            // check what to do with the data
            // if WLAN is available or not neccessary, send data
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String auth_token = sharedPrefs.getString("auth_token", "");
            Log.i("TourManagerService", "auth_token: " + auth_token);
            boolean connected = !auth_token.equals("");
            if (connected && (WiFiAvailable() || LiveUploadChecked())) {
                //Log.i("Status", "***********" + connected + "***" + WiFiAvailable() + "***" + LiveUploadChecked());
                //check if number of wayPoints is enough to send data
                //Log.i("Status2", "*******"  + tour.GetWayPoints().size() + "***" + tour.GetTourComplete());
                if (tour.GetWayPoints().size() >= queueLength) {
                    // send data and clear waypoint list in tour
                    Tour t = ClearWayPoints();
                    Log.i("TourManagerService", "Daten werden gesendet: " + t.toJSON().toString());
                    ((MainActivity)context).LogDBState("vor  send: ");
                    RequestManager.getInstance().doRequest().SendTourData(auth_token, t);
                    ((MainActivity)context).LogDBState("nach send: ");
                }
                else
                    Log.i("TourManagerService", "nichts gesendet, da queue nicht voll");
            }
            else {
                Log.i("TourManagerService", "nichts gesendet, da keine erforderliche Verbindung");
                ((MainActivity) context).LogDBState("ohne send: ");
            }
        }
        else
            Log.i("TourManagerService", "invalid location");
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

    // returns the duration since the tour started in ms
    public long GetDuration_ms()
    {
        long duration_ms;

        if (use_filtered_values)
            duration_ms = duration_ms_filtered;
        else
            duration_ms = duration_ms_all;

        Log.i("TourManagerService", "GetDuration_ms: " + duration_ms + " ms");
        return duration_ms;
    }

    // returns the distance travelled since the tour started in km
    public double GetDistance_km() {
        double distance_km;

        if (use_filtered_values)
            distance_km = distance_m_filtered / 1000f;
        else
            distance_km = distance_m_all / 1000f;

        Log.i("TourManagerService", "GetDistance_kmh: " + distance_km + " km");
        return distance_km;
    }

    // returns the current speed in km/h
    public double GetCurrentSpeed_kmh() {
        Log.i("TourManagerService", "GetCurrentSpeed: " + speed + " km/h");
        return speed;
    }

    // returns the average speed in km/h
    public double GetAverageSpeed_kmh() {
        double distance_m;
        double duration_ms;
        double speed_kmh;

        if (use_filtered_values) {
            distance_m = distance_m_filtered;
            duration_ms = duration_ms_filtered;
        } else {
            distance_m = distance_m_all;
            duration_ms = duration_ms_all;
        }

        if (duration_ms != 0)
            speed_kmh = (distance_m * 3600) / ((double) duration_ms);
        else
            speed_kmh = 0;

        Log.i("TourManagerService", "GetAverageSpeed: " + speed_kmh + " km/h");
        return speed_kmh;
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

    /**
     * Gibt die aktuellen Tourdaten zurück
     * @return
     */
    public TourData getTourData ()
    {
        TourData tourData = new TourData(
                GetDistance_km(),
                GetDuration_ms(),
                GetCurrentSpeed_kmh(),
                GetAverageSpeed_kmh()
        );
        return tourData;
    }

    /**
     * Observer-Methode wird von außen aufgerufen
     */
    @Override
    public void update(Observable observable, Object data) {
        Log.i("TourManagerService", "update");
        try {
            AddWayPoint((Location) data);
            TourData tourData = new TourData(
                    GetDistance_km(),
                    GetDuration_ms(),
                    GetCurrentSpeed_kmh(),
                    GetAverageSpeed_kmh()
            );
            notifyListeners(tourData);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    /**
     * Für eigenes Observer-Pattern
     */
    public void registerListener(Observer listener)
    {
        Log.i("TourManagerService", "registerListener");
        this.listener = listener;
    }

    /**
     * Benachrichtige Listener
     */
    private void notifyListeners(TourData tourData){
        try {
            if (listener != null) {
                Log.i("TourManagerService", "notifyListeners");
                listener.update(null, tourData);
            }
        }
        catch (Exception e)
        {
            Log.e("TourManagerService", "TourManagerService konnte UI nicht benachrichtigen");
        }
    }

    /**
     * Listener entfernen
     */
    public void deregisterListener(){
        Log.i("TourManagerService", "deregisterListener");
        this.listener = null;
    }


}
