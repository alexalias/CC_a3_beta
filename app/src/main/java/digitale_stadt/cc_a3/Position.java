package digitale_stadt.cc_a3;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alexutza_a on 29.04.2016.
 * Implements a Position
 */
public class Position {
    final String json_id_id = "cmt";
    final String json_id_lat = "lat";
    final String json_id_lon = "lon";
    final String json_id_alt = "ele";
    final String json_id_time = "time";

    private long id;
    private String tourID;
    private Date time;
    private double latitude;
    private double longitude;
    private double altitude;
    private int sent;

    //Standardkonstruktor
    public Position() {
    }

    //Konstruktor
    public Position(String tourID, long positionID, Location location){
        this(tourID, positionID, location.getTime(), location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    //Konstruktor
    public Position(String tourID, long positionID, long time, double lat, double lon, double alt){
        this.tourID = tourID;
        this.id = positionID;
        this.time = new Date(time);
        this.latitude = lat;
        this.longitude = lon;
        this.altitude = alt;
        sent = 0;
    }

    public Position(JSONObject json)
    {
        try {
            //JSONObject json = new JSONObject(JSONString);
            this.id = json.optLong(json_id_id);
            this.time = new Date();
            this.latitude = json.optLong(json_id_lat);
            this.longitude = json.optLong(json_id_lon);
            this.altitude = json.optLong(json_id_alt);
        }
        catch (Exception e)
        {
        }
    }

    // Getter und Setter
    public int getSent() {return sent;}
    public void setSent(int sent) {this.sent = sent;}

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public String getTourID() {return tourID;}
    public void setTourID(String tourID) {this.tourID = tourID;}

    public Date getTime() {return time;}
    public void setTime(Date time) {this.time = time;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}

    public double getAltitude() {return altitude;}
    public void setAltitude(double altitude) {this.altitude = altitude;}

    //changes Flag
    public void changeSent() {
        if (sent == 0) {sent = 1;}
        else {sent = 0;}
    }

    public JSONObject toJSON()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put(json_id_id, id);

            String strCurrentDate = "Wed, 18 Apr 2012 07:55:29 +0000";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
            String date = format.format(time);
            jsonObject.put(json_id_time, date);

            jsonObject.put(json_id_lat, latitude);
            jsonObject.put(json_id_lon, longitude);
            jsonObject.put(json_id_alt, altitude);

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
