package digitale_stadt.cc_a3;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Observable;

/**
 * Created by anne on 01.03.16.
 */
public class GPSTrackerObject extends Observable{

    private LocationManager manager;
    private LocationListener locationListener;

    private float fullDistance = 0f;

    private double latLast, lonLast;
    private Location lastLocation;

    private Location nullLocation;

    public GPSTrackerObject() {

        Log.i("GPSTracker", "Bin im GPSTracker und erstelle den manager + location");

        //ToDo: Hier ist der Context immer noch NULL !!!
        manager = (LocationManager) Config.mContext.getSystemService(Context.LOCATION_SERVICE);

        nullLocation = new Location("empty");
        nullLocation.setLatitude(0.0);
        nullLocation.setLongitude(0.0);
        Log.i("!?GPSTracker", "locationListener wird gesetzt");
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try{
                    Log.i("!GPSTracker", "Hole neue Location");

                    Location l = new Location("last");
                    l.setLatitude(latLast);
                    l.setLongitude(lonLast);


                    if(!(l.getLatitude() == nullLocation.getLatitude() && l.getLongitude() == nullLocation.getLongitude())) {
                        Log.i("!?GPSTracker", "Locations sind unterschiedlich, also berechne die distance drauf");
                        fullDistance += location.distanceTo(l);
                    }

                    latLast = location.getLatitude();
                    lonLast = location.getLongitude();
                    lastLocation = location;

                }catch (Exception e){
                    e.printStackTrace();
                }
                newPosition();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String paddressrovider) {

            }
        };

        try {
            Log.i("!?GPSTracker", "Manager RequestLocationUpdate wir angemacht mit 3 Sekunden abstand");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000L, 0, locationListener);
        }catch (SecurityException e){

        }

    }

    private void newPosition()
    {
        Log.i("!?GPSTracker", "Bin in der newPosition -> sette die Change und notify die Observers");
        setChanged();
        notifyObservers();
    }

    public float getDistanceDrivenInMeters()
    {
        return fullDistance;
    }

    public Location getLastLocation()
    {
        return lastLocation;
    }

    public void destroyListener()
    {
        try {
            manager.removeUpdates(locationListener);
            Log.i("!?GPSTracker", "Listener wird destroyt, also removeUpdates");
        }
        catch (SecurityException e)
        {

        }
    }
}