package digitale_stadt.cc_a3;

import android.location.Location;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ralf Engelken on 16.05.2016.
 * Tests the TourManager
 */
public class TourManagerTest {
    TourManager tourManager;
    Location loc1, loc2, loc3;

    @Before
    public void Before()
    {
        tourManager = new TourManager(AppContextProvider.getAppContext(), "001") {
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
        };
        tourManager.StartNewTour();
    }

    @Test
    public void AddWaypointTest() {
        loc1 = new Location("?");
        loc1.setLatitude(4.5);
        loc1.setLongitude(3.8);
        loc1.setAltitude(5.5);
        loc1.setTime(100000);
        loc2 = new Location("?");
        loc2.setLatitude(4.2);
        loc2.setLongitude(3.1);
        loc2.setAltitude(5.8);
        loc2.setTime(100004);
        loc3 = new Location("?");
        loc3.setLatitude(4.5);
        loc3.setLongitude(3.8);
        loc3.setAltitude(5.5);
        loc3.setTime(100008);

        tourManager.SetQueueLength(5);

        assertEquals(0, tourManager.GetTour().GetWayPoints().size());
//        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
//        assertEquals(0.0, tourManager.GetDuration_ms(), 0.1);
//        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        tourManager.AddWayPoint(loc1);
//        assertEquals(1, tourManager.GetTour().GetWayPoints().size());
//        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
//        assertEquals(0.0, tourManager.GetDuration_ms(), 0.1);
//        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        tourManager.AddWayPoint(loc2);
//        assertEquals(2, tourManager.GetTour().GetWayPoints().size());
//        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
//        assertEquals(0.0, tourManager.GetDuration_ms(), 0.1);
//        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);

        tourManager.AddWayPoint(loc3);
//        assertEquals(3, tourManager.GetTour().GetWayPoints().size());
//        assertEquals(0.0, tourManager.GetDistance_km(), 0.1);
//        assertEquals(0.0, tourManager.GetDuration_ms(), 0.1);
//        assertEquals(0.0, tourManager.GetAvgSpeed_kmh(), 0.1);
    }
}
