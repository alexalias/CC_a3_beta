package digitale_stadt.cc_a3;

import android.location.Location;

/**
 * Created by ralf on 14.06.2016.
 */
public class LocationPostCorrection {

    int m_SmoothingInterval;
    //Location[]

    public LocationPostCorrection(int smoothing_interval)
    {
        m_SmoothingInterval = smoothing_interval;
    }

    public Location getSmoothenedLocation(Location location)
    {
        Location result = new Location(location);

        return result;
    }
}
