package digitale_stadt.cc_a3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ralf Engelken on 02.05.16.
 */
public class Tour {
    private String user;
    private String pw;
    private int tourid;
    private List<Position> WayPoints;

    public Tour(String user, String pw, int tourid)
    {
        this.user = user;
        this.pw= pw;
        this.tourid = tourid;
        WayPoints = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public String getPw() {
        return pw;
    }

    public int getTourid() {
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

    public String getJSONString()
    {
        String json;

        //Build JSON object
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put("user", user);
            jsonObject.put("pw", pw);
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
