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

        Log.i("GPSTrackerService", "Show die Notification");
        showNotification("");
        return super.onStartCommand(intent, START_STICKY, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.i("GPSTrackerService", "onBind, erstelle Object und sei der Observer");
        gps = new GPSTrackerObject();
        gps.addObserver(this);

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("GPSTrackerService", "onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i("GPSTrackerService", "onDestroy, entferne dich als Observer und die Listeners beim observable");
        gps.deleteObserver(this);
        gps.destroyListener();

        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i("GPSTrackerService", "update");
        //distance = gps.getDistanceDrivenInMeters();
        lastLocation = gps.getLastLocation();

        //showNotification(getDrivenDistanceInKilometers() + "km gefahren");

        notifyListeners(lastLocation);
    }


    /**
     * Zeigt eine permanente Notification an, solange der Service läuft
     */
    private void showNotification(String text) {
        Log.i("GPSTrackerService", "showNotification");
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
        Log.i("GPSTrackerService", "buildNotification");
//        return new Notification.Builder(this)
//                .setContentTitle("")
//                .setContentText(content)
//                .setContentIntent(pIntent)
//                .setOngoing(true)
//                .setAutoCancel(false).build();
        return new Notification();
    }

    /**
     * Für eigenes Observer-Pattern
     */
    public void registerListener(Observer listener)
    {
        Log.i("GPSTrackerService", "registerListener");
        this.listener = listener;
    }

    /**
     * Benachrichtige Listener
     */
    private void notifyListeners(Location location){
        try {
            if (listener != null) {
                Log.i("GPSTrackerService", "notifyListeners");
                listener.update(null, location);
            }
        }
        catch (Exception e)
        {
            Log.e("GPSTrackerService", "notifyListeners: konnte listener nicht benachrichtigen");
        }
    }

    /**
     * Listener entfernen
     */
    public void deregisterListener(){
        Log.i("GPSTrackerService", "deregisterListener");
        this.listener = null;
    }
}
