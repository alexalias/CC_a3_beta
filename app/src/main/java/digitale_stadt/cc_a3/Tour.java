package digitale_stadt.cc_a3;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Ralf Engelken on 02.05.16.
 * Implements a Tour
 */
public class Tour {
    final String json_id_tourID = "tourid";
    final String json_id_WayPoints = "WayPoints";

    private String tourID;
    private List<Position> WayPoints;
    private boolean tourComplete;

    public Tour(Tour tour)
    {
        tourID = tour.tourID;
        WayPoints = new ArrayList<>(tour.WayPoints);
        tourComplete = tour.tourComplete;
    }

    public Tour(String deviceID)
    {
        this.tourID = Hasher.md5(new Date().toString() + deviceID);
        WayPoints = new ArrayList<>();
    }

    public Tour(String deviceID, Date date)
    {
        this.tourID = Hasher.md5(date.toString() + deviceID);
        WayPoints = new ArrayList<>();
    }

    public Tour(String tourID, ArrayList<Position> wayPointList)
    {
        this.tourID = tourID;
        WayPoints = new ArrayList<>();
        for (Position pos : wayPointList)
        {
            WayPoints.add(pos);
        }
    }

    public String getTourID() {
        return tourID;
    }

    public List<Position> GetWayPoints() {
        return WayPoints;
    }

    public void AddWayPoint(Position position)
    {
        if(position != null)
            WayPoints.add(position);
    }

    public void AddWayPoint(Location location)
    {
        long id = 0;
        if (location != null)
        {
            if (WayPoints.size() > 0)
                id = WayPoints.get(WayPoints.size()-1).getId()+1;
            Position position = new Position(tourID, id, location);

            WayPoints.add(position);
        }
    }

    public Position GetStartPosition()
    {
        return WayPoints.get(0);
    }

    public Position GetEndPosition()
    {
        if (WayPoints.size() > 0) {
            Position pos = WayPoints.get(WayPoints.size() - 1);
            if (pos.getId() == -1)
                return pos;
            else
                return null;
        }
        else
            return null;
    }

    public Position GetLastPosition()
    {
        if (WayPoints.size() > 0)
            return WayPoints.get(WayPoints.size()-1);
        else
            return null;
    }

    public void ClearWayPoints()
    {
        WayPoints.clear();
    }

    //return the JSON-Object for the tour
    public JSONObject toJSON()
    {
        String json;

        //Build JSON object
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            jsonObject.put(json_id_tourID, tourID);
            //jsonObject.put("WayPoints", WayPoints);
            for (Position pos : WayPoints)
            {
                jsonArray.put(pos.toJSON());
            }
            jsonObject.put(json_id_WayPoints, jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Convert jsonObject to String
        json = jsonObject.toString();

        return jsonObject;
    }

    public boolean GetTourComplete() {
        return tourComplete;
    }

    public void SetTourComplete(boolean tourComplete) {
        this.tourComplete = tourComplete;
    }
}
