package digitale_stadt.cc_a3;

import android.location.Location;

/**
 * Created by 1engelke on 14.06.2016.
 *
 */
public class LocationPostCorrection {

    int m_SmoothingInterval;
    int m_Counter;
    Location[] m_Locations;

    public LocationPostCorrection(int smoothing_interval)
    {
        m_SmoothingInterval = smoothing_interval;
        m_Locations = new Location[m_SmoothingInterval];
        m_Counter = -1;
    }

    public Location getSmoothenedLocation(Location location)
    {
        if (location != null) {
            //clone given location and set lat/lon and alt to 0
            Location result = new Location(location);

            double lat = 0.0;
            double lon = 0.0;
            double alt = 0.0;

            if (m_Counter < 0) {
                m_Counter = 0;
                //initialize array with first given location
                for (int i = 0; i < m_SmoothingInterval; i++)
                    m_Locations[i] = location;
            } else {
                //set m_Counter to next index using Round Robin and save given location at the new index
                m_Counter = (m_Counter + 1) % m_SmoothingInterval;
                m_Locations[m_Counter] = location;
            }

            for (Location loc : m_Locations) {
                lat += loc.getLatitude();
                lon += loc.getLongitude();
                alt = loc.getAltitude();
            }

            result.setLatitude(lat / 3);
            result.setLongitude(lon / 3);
            result.setAltitude(alt / 3);

            return result;
        }
        else
            return null;
    }
}
