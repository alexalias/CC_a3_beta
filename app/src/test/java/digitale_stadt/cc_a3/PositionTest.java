package digitale_stadt.cc_a3;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class PositionTest {

    private Tour m_Tour;
    private Position m_Position;

    private final double lat = 4.0;
    private final double lon = 5.0;
    private final double ele = 6.0;
    private final long time = 999999;

    private String jsonString = "{\"lon\":5,\"time\":\"1970-01-01 01:16:39\",\"cmt\":0,\"lat\":4,\"ele\":6}";

    @Before
    public void Before()
    {
        m_Tour = new Tour("001", new Date(time));

        m_Position = new Position(m_Tour.getTourID(), 0, time, lat, lon, ele);
    }

    @Test
    public void InitializationTest() {
        assertEquals(new Date(time), m_Position.getTime());
        assertEquals(m_Tour.getTourID(), m_Position.getTourID());

        assertEquals(lat, m_Position.getLatitude(), 0.01);
        assertEquals(lon, m_Position.getLongitude(), 0.01);
        assertEquals(ele, m_Position.getAltitude(), 0.01);
    }

    @Test
    public void toJSONStringTest()
    {
        String compareString = m_Position.toJSON().toString();
        assertEquals(jsonString, compareString);
    }

//    @Test
//    public void fromJSONStringTest()
//    {
//        Position pos = new Position(jsonString);
//        assertEquals(lat, pos.getLatitude(), 0.1);
//        assertEquals(lon, pos.getLongitude(), 0.1);
//        assertEquals(ele, pos.getAltitude(), 0.1);
//    }
}
