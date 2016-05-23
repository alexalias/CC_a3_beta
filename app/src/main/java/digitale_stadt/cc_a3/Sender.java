package digitale_stadt.cc_a3;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by alexutza_a on 29.04.2016.
 * Implements a Sender
 */
public class Sender {
    private RequestQueue queue;
    final String default_url = "https://preview.api.cycleyourcity.jmakro.de:4040/log_coordinates.php";
    Context _context;

    public Sender(Context context)
    {
        queue = Volley.newRequestQueue(context);
        _context = context;
    }

    public void SendTourData(Tour tour)
    {
        SendJSONString(default_url, tour.toJSON().toString());
    }

    public void SendTourData(String url, Tour tour)
    {
        SendJSONString(url, tour.toJSON().toString());
    }

    public void SendJSONString (String url, String s)
    {
        HashMap<String,String> map = new HashMap<>();
        map.put("data", s);

        if(WiFiAvailable()) {
            Log.i("MAIN", "Sending " + s);
            SendPostRequest(url, map);
        }
    }

    private void SendPostRequest(String url, final Map<String, String> params)
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.i("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        queue.add(postRequest);
    }

    public boolean WiFiAvailable(){
        ConnectivityManager cm =
                (ConnectivityManager)_context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    // prüft ob 'datenupload nur bei WLAN' in den shared prefs true ist
    public boolean wlanUploadChecked(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(_context);
        return sharedPrefs.getBoolean("wlan_upload", false);
    }

}
