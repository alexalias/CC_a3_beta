package digitale_stadt.cc_a3;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by alexutza_a on 29.04.2016.
 */
public class Position {
    private long id;
    private int tourID;
    private Date time;
    private double latitude;
    private double longitude;
    private double altitude;
    private int sent;

    //Standardkonstruktor
    public Position() {
    }

    //Konstruktor
    public Position(Tour tour, Location location){
        this.tourID = tour.getTourID();
        time = new Date(location.getTime());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        sent = 0;
    }

    // Getter und Setter
    public int getSent() {return sent;}
    public void setSent(int sent) {this.sent = sent;}

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public int getTourID() {return tourID;}
    public void setTourID(int tourID) {this.tourID = tourID;}

    public Date getTime() {return time;}
    public void setTime(Date time) {this.time = time;}

    public double getLatitude() {return latitude;}
    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}
    public void setLongitude(double longitude) {this.longitude = longitude;}

    public double getAltitude() {return altitude;}
    public void setAltitude(double altitude) {this.altitude = altitude;}

    public JSONObject getJSONObject()
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.accumulate("cmt", id);
            jsonObject.accumulate("time", time);
            jsonObject.accumulate("lat", latitude);
            jsonObject.accumulate("lon", longitude);
            jsonObject.accumulate("ele", altitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
