package digitale_stadt.cc_a3;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ralf Engelken on 14.05.2016.
 * Tests the Tour
 */
public class TourTest {

    private Tour m_Tour;
    private final long time = 190729999000L;

    @Before
    public void Before() {
        m_Tour = new Tour("ff8c68734c6f0071426e10d9a7b81039", new ArrayList<Position>());
    }

    @Test
    public void InitializationTest() {
        assertEquals(0, m_Tour.GetWayPoints().size());

        String compareString = "ff8c68734c6f0071426e10d9a7b81039";
        assertEquals(compareString, m_Tour.getTourID());
    }

    @Test
    public void AddWayPointTest()
    {
        assertEquals(0, m_Tour.GetWayPoints().size());

        Position pos = new Position(m_Tour.getTourID(), 0, time+2, 4.0, 5.0, 6.0);
        m_Tour.AddWayPoint(pos);

        assertEquals(1, m_Tour.GetWayPoints().size());

        pos = new Position(m_Tour.getTourID(), 1, time+4, 4.0, 5.0, 6.0);
        m_Tour.AddWayPoint(pos);

        assertEquals(2, m_Tour.GetWayPoints().size());
    }

    @Test
    public void JSONStringTest()
    {
        String jsonString = m_Tour.toJSON().toString();
        String compareString = "{\"WayPoints\":[],\"tourid\":\"ff8c68734c6f0071426e10d9a7b81039\"}";
        assertEquals(jsonString, compareString);

        Position pos = new Position(m_Tour.getTourID(), 0, time+2, 4.0, 5.0, 6.0);
        m_Tour.AddWayPoint(pos);

        jsonString = m_Tour.toJSON().toString();
        compareString = "{\"WayPoints\":[{\"lon\":5,\"time\":\"17-01-1976 13-33-19\",\"cmt\":0,\"lat\":4,\"ele\":6}],\"tourid\":\"ff8c68734c6f0071426e10d9a7b81039\"}";
        assertEquals(jsonString, compareString);
    }
}