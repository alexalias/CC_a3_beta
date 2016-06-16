package digitale_stadt.cc_a3;

import android.location.Location;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class LocationPostCorrectionTest {

    private LocationPostCorrection lpc;

    @Mock
    Location loc1, loc2, loc3;

    private final double lat = 4.0;
    private final double lon = 5.0;
    private final double ele = 6.0;
    private final long time = 999999;

    @Before
    public void Before()
    {
        lpc = new LocationPostCorrection(3);
    }

    @Test
    public void InitializationTest() {

    }

    @Test
    public void SmootheningTest()
    {
        Location loc;

        when(loc1.getLatitude()).thenReturn(30.0);
        when(loc1.getLongitude()).thenReturn(50.0);
        when(loc1.getAltitude()).thenReturn(5.0);

        when(loc2.getLatitude()).thenReturn(35.0);
        when(loc2.getLongitude()).thenReturn(45.0);
        when(loc2.getAltitude()).thenReturn(6.0);

        when(loc3.getLatitude()).thenReturn(40.0);
        when(loc3.getLongitude()).thenReturn(40.0);
        when(loc3.getAltitude()).thenReturn(7.0);

        loc = lpc.getSmoothenedLocation(loc1);
        assertEquals(30.0, loc.getAltitude(), 1.0);
    }
}
