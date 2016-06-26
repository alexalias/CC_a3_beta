package digitale_stadt.cc_a3;

import android.location.Location;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by Ralf Engelken on 16.05.2016.
 * Tests the TourManager
 */
@RunWith(MockitoJUnitRunner.class)
public class TourManagerTest {

//    public class Location
//    {
//        double lat;
//        double lon;
//        double ele;
//        long time;
//
//        public Location(String s)
//        {
//
//        }
//
//        public double getLatitude() { return lat; }
//        public double getLongitude() { return lon; }
//        public double getAltitude() { return ele; }
//        public long getTime() { return time; }
//
//        public void setLatitude(double d) { lat = d; }
//        public void setLongitude(double d) { lon = d; }
//        public void setAltitude(double d) { ele = d; }
//        public void setTime(long t) { time = t; }
//    }

    TourManager tourManager;
    @Mock
    Location loc1, loc2, loc3;

    public double radiansFromDegrees(double degrees)
    {
        return degrees * (Math.PI/180.0);
    }

    public double degreesFromRadians(double radians)
    {
        return radians * (180.0/Math.PI);
    }

    public Location locationFromLocation(Location fromLocation, double distance, double bearingDegrees)
    {
        double distanceKm = distance / 1000.0;
        double distanceRadians = distanceKm / 6371.0;
        //6,371 = Earth's radius in km
        double bearingRadians = radiansFromDegrees(bearingDegrees);
        double fromLatRadians = radiansFromDegrees(fromLocation.getLatitude());
        double fromLonRadians = radiansFromDegrees(fromLocation.getLongitude());

        double toLatRadians = Math.asin( Math.sin(fromLatRadians) * Math.cos(distanceRadians)
                + Math.cos(fromLatRadians) * Math.sin(distanceRadians) * Math.cos(bearingRadians) );

        double toLonRadians = fromLonRadians + Math.atan2(Math.sin(bearingRadians)
                * Math.sin(distanceRadians) * Math.cos(fromLatRadians), Math.cos(distanceRadians)
                - Math.sin(fromLatRadians) * Math.sin(toLatRadians));

        // adjust toLonRadians to be in the range -180 to +180...
        toLonRadians = ((toLonRadians + 3*Math.PI) % (2*Math.PI) ) - Math.PI;

        //Location result = new Location(LocationManager.GPS_PROVIDER);
        Location result = new Location("");
        result.setLatitude(degreesFromRadians(toLatRadians));
        result.setLongitude(degreesFromRadians(toLonRadians));
        return result;
    }

    public double[] coordsFromLocation(Location fromLocation, double distance, double bearingDegrees)
    {
        double distanceKm = distance / 1000.0;
        double distanceRadians = distanceKm / 6371.0;
        //6,371 = Earth's radius in km
        double bearingRadians = radiansFromDegrees(bearingDegrees);
        double fromLatRadians = radiansFromDegrees(fromLocation.getLatitude());
        double fromLonRadians = radiansFromDegrees(fromLocation.getLongitude());

        double toLatRadians = Math.asin( Math.sin(fromLatRadians) * Math.cos(distanceRadians)
                + Math.cos(fromLatRadians) * Math.sin(distanceRadians) * Math.cos(bearingRadians) );

        double toLonRadians = fromLonRadians + Math.atan2(Math.sin(bearingRadians)
                * Math.sin(distanceRadians) * Math.cos(fromLatRadians), Math.cos(distanceRadians)
                - Math.sin(fromLatRadians) * Math.sin(toLatRadians));

        // adjust toLonRadians to be in the range -180 to +180...
        toLonRadians = ((toLonRadians + 3*Math.PI) % (2*Math.PI) ) - Math.PI;

//        result.setLatitude(degreesFromRadians(toLatRadians));
//        result.setLongitude(degreesFromRadians(toLonRadians));
        double[] result = new double[] {degreesFromRadians(toLatRadians), degreesFromRadians(toLonRadians)};

        return result;
    }

    @Before
    public void Before()
    {
        tourManager = new TourManager(AppContextProvider.getAppContext(), "001") {
//            @Override
//            public boolean StartServices() {
//                Log.d("Initialize Services", "");
//                return true;
//            }

            @Override
            public boolean SaveTourToDB() {
                for (Position pos : tour.GetWayPoints())
                    Log.d("Saving Position", pos.toJSON().toString());
                return true;
            }

            @Override
            public boolean SendTourToServer() {
                Log.d("Sending Tour", tour.toJSON().toString());
                return true;
            }

            @Override
            public boolean WiFiAvailable (){
                Log.d("Checking WiFi", "");
                return true;
            }
        };
    }

    @Test
    public void InitialisationTest() {
        tourManager.use_filtered_values = true;
        assertEquals(0, tourManager.GetDuration_ms());
        assertEquals(0, tourManager.GetDistance_km(), 0.01);

        tourManager.use_filtered_values = false;
        assertEquals(0, tourManager.GetDuration_ms());
        assertEquals(0, tourManager.GetDistance_km(), 0.01);
    }

    @Test
    public void AddWaypointTest() {
        double[] lat_lon = {};

        // Given a mocked Context injected into the object under test...
//        when(loc1.getLatitude()).thenReturn(4.5);
//        when(loc1.getLongitude()).thenReturn(3.8);
//        when(loc1.getAltitude()).thenReturn(0d);
        when(loc1.getTime()).thenReturn(100000L);
        when(loc1.distanceTo(loc1)).thenReturn(0f);

//        lat_lon = coordsFromLocation(loc1, 100, 0);
//        lat_lon = new double[] {5.3, 4.2};
//        when(loc2.getLatitude()).thenReturn(lat_lon[0]);
//        when(loc2.getLongitude()).thenReturn(lat_lon[1]);
//        when(loc2.getAltitude()).thenReturn(0d);
        when(loc2.getTime()).thenReturn(220000L);
        when(loc2.distanceTo(loc1)).thenReturn(1000f);

//        lat_lon = coordsFromLocation(loc1, 200, 0);
//        lat_lon = new double[] {6.5, 3.8};
//        when(loc3.getLatitude()).thenReturn(lat_lon[0]);
//        when(loc3.getLongitude()).thenReturn(lat_lon[1]);
//        when(loc3.getAltitude()).thenReturn(0d);
        when(loc3.getTime()).thenReturn(400000L);
        when(loc3.distanceTo(loc2)).thenReturn(1500f);

        tourManager.SetQueueLength(5);
        //empty tour
        assertEquals(0, tourManager.GetTour().GetWayPoints().size());
        assertEquals(0, tourManager.GetDuration_ms());
        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        //add first waypoint: no distance and time
        tourManager.AddWayPoint(loc1);
        assertEquals(1, tourManager.GetTour().GetWayPoints().size());
        assertEquals(0, tourManager.GetDuration_ms());
        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        //add 2nd waypoint: 1000m in 120s
        tourManager.AddWayPoint(loc2);
        assertEquals(2, tourManager.GetTour().GetWayPoints().size());
        assertEquals(120000L, tourManager.GetDuration_ms());
        assertEquals(1.0, tourManager.GetDistance_km(), 0.1);
        assertEquals(30.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        ///add 3rd waypoint: 1500m in 180s -> overall 2500m in 300s
        tourManager.AddWayPoint(loc3);
        assertEquals(3, tourManager.GetTour().GetWayPoints().size());
        assertEquals(300000L, tourManager.GetDuration_ms());
        assertEquals(2.5, tourManager.GetDistance_km(), 0.1);
        assertEquals(30.0, tourManager.GetAvgSpeed_kmh(), 0.1);
    }
}
