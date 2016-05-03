package digitale_stadt.cc_a3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ralf Engelken on 02.05.16.
 */
public class Tour {
    private int tourid;
    private List<Position> WayPoints;

    public Tour()
    {
        tourid = new Date().hashCode();
        WayPoints = new ArrayList<>();
    }

    public int getTourID() {
        return tourid;
    }

    public List<Position> getWayPoints() {
        return WayPoints;
    }

    public void AddWayPoint(Position position)
    {
        if(position != null)
            WayPoints.add(position);
    }

    //return the JSON-Stringfo the tour
    public String getJSONString()
    {
        String json;

        //Build JSON object
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put("tourid", tourid);
            //jsonObject.put("WayPoints", WayPoints);
            for (Position pos : WayPoints)
            {
                jsonArray.put(pos.getJSONObject());
            }
            jsonObject.put("WayPoints", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Convert jsonObject to String
        json = jsonObject.toString();

        return json;
    }
}
