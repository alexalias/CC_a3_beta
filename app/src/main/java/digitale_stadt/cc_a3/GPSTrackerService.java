package digitale_stadt.cc_a3;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

public class GPSTrackerService extends Service implements Observer{

    private final IBinder mBinder = new GpsBinder();
    private Notification n;
    private int NOTIFICATION_ID = 1000;
    private Observer listener;
    private float distance = 0f;
    private Location lastLocation;
    private GPSTrackerObject gps;

    public class GpsBinder extends Binder{

        GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("GPSTrackerService", "Show die Notification");
        showNotification("");
        return super.onStartCommand(intent, START_STICKY, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d("GPSTrackerService", "bei Binde, erstelle Object und sei der Observer");
        gps = new GPSTrackerObject();
        gps.addObserver(this);

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.d("!?GPSTrackerService", "Unbinde dich");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d("!?GPSTrackerService", "Delete dich als Observer und destroy die Listeners beim observable");
        gps.deleteObserver(this);
        gps.destroyListener();

        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d("GPSTrackerService", "neue location");
        //distance = gps.getDistanceDrivenInMeters();
        lastLocation = gps.getLastLocation();

        //showNotification(getDrivenDistanceInKilometers() + "km gefahren");

        notifyListeners(lastLocation);
    }


    /**
     * Zeigt eine permanente Notification an, solange der Service läuft
     */
    private void showNotification(String text) {
        Log.d("!?GPSTrackerService", "Showe die Notification, builde die notification");
        Intent tourView = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), tourView, 0);

        n  = buildNotification(text, pIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        startForeground(NOTIFICATION_ID, n);
    }

    /**
     * Baut die Notification
     */
    private Notification buildNotification(String content, PendingIntent pIntent){
        Log.d("!?GPSTrackerService", "Builde die notifcation");
//        return new Notification.Builder(this)
//                .setContentTitle("")
//                .setContentText(content)
//                .setContentIntent(pIntent)
//                .setOngoing(true)
//                .setAutoCancel(false).build();
        return new Notification();
    }


    public float getDrivenDistanceInMeters(){
        Log.d("!?GPSTrackerService", "returne die Distance");
        return distance;
    }

    public int getDrivenDistanceInMetersRounded()
    {
        Log.d("!?GPSTrackerService", "returne die Distance in Meters");
        return (int) getDrivenDistanceInMeters();
    }

    public Location getLastLocation()
    {
        return lastLocation;
    }

    /**
     * Liefert einen formatierten String mit einer Nachkommastelle.
     */
    public String getDrivenDistanceInKilometers()
    {
        Log.d("!?GPSTrackerService", "returne die Distance in Kilometer");
        return String.format("%.1f", getDrivenDistanceInMeters() / 1000) ;
    }

    /**
     * Für eigenes Observer-Pattern
     */
    public void registerListener(Observer listener)
    {
        Log.d("GPSTrackerService", "setze Listener");
        this.listener = listener;
    }

    /**
     * Benachrichtige Listener
     */
    private void notifyListeners(Location location){
        try {
            if (listener != null) {
                Log.d("!?GPSTrackerService", "Listeners werden upgedatet");
                listener.update(null, location);
            }
        }
        catch (Exception e)
        {
            Log.e("TrackerService", "TrackerService konnte UI nicht benachrichtigen");
        }
    }

    /**
     * Listener entfernen
     */
    public void deregisterListener(){
        Log.d("GPSTrackerService", "Register = null also deregister");
        this.listener = null;
    }
}
