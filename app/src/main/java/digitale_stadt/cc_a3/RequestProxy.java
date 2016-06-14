package digitale_stadt.cc_a3;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lorcan on 25.05.2016.
 */
public class RequestProxy {

    private RequestQueue mRequestQueue;

    final String login_url = "https://api.cyc.jmakro.de:4040/get_auth_token.php";
    final String data_url = "https://api.cyc.jmakro.de:4040/log_coordinates.php";

    final String server_login_failed = "wrong credentials";
    final String server_data_transmission_token_not_valid = "auth_token not set";

    RetryPolicy defaultRetryPolicy = new DefaultRetryPolicy(
        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,   // 1000
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,  // 1
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT  //1f
    );

    // package access constructor
    RequestProxy(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void Login(final String login_url, final String username, final String password)
    {
        Log.i("RequestProxy", "Connecting with " + username + " / " + password);
        StringRequest postRequest = new StringRequest(Request.Method.POST, login_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        if (response.equals(server_login_failed))
                        {
                            Log.i("Login Response", "Login rejected: " + response);
//                            _token = "Test_ohne_login";
//                            ((MainActivity)_context).UpdateDebugInfo(" F ");
//                            Toast.makeText(RequestProxy.this, "Login OK", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.i("Login Response", "Token received: " + response);
                            try {
                                JSONObject myJson = new JSONObject(response);
                                // use myJson as needed, for example
                                String name = myJson.optString("name");
                                String token = myJson.optString("auth_token");
                                // etc
//                                _token = token;
//                                ((MainActivity)_context).UpdateDebugInfo(" L ");
                            }
                            catch (Exception e)
                            {

                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.i("Login Error.Response", error.toString());
//                        _token = "";
//                        ((MainActivity)_context).UpdateDebugInfo(" F ");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };

        //set timeout to 1000ms and retries to 1
        postRequest.setRetryPolicy(defaultRetryPolicy);

        mRequestQueue.add(postRequest);
    }

    public void Register(final String login_url, final String username, final String password, final String email)
    {
        Log.i("RequestProxy", "Register with " + username + " / " + password);
        StringRequest postRequest = new StringRequest(Request.Method.POST, login_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        if (response.equals(server_login_failed))
                        {
                            Log.i("Register Response", "Login rejected: " + response);
//                            _token = "Test_ohne_login";
//                            ((MainActivity)_context).UpdateDebugInfo(" F ");
//                            Toast.makeText(RequestProxy.this, "Login OK", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.i("Register Response", "Token received: " + response);
//                            Toast.makeText(RequestProxy.this, "Login FAILED", Toast.LENGTH_SHORT).show();
                            try {
                                JSONObject myJson = new JSONObject(response);
                                // use myJson as needed, for example
                                String name = myJson.optString("name");
                                String token = myJson.optString("auth_token");
                                // etc
//                                _token = token;
//                                ((MainActivity)_context).UpdateDebugInfo(" L ");
                            }
                            catch (Exception e)
                            {

                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.i("Register Error.Response", error.toString());
//                        _token = "";
//                        ((MainActivity)_context).UpdateDebugInfo(" F ");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                params.put("email", email);
                return params;
            }
        };

        //set timeout to 1000ms and retries to 1
        postRequest.setRetryPolicy(defaultRetryPolicy);
        mRequestQueue.add(postRequest);
    }

    public void Register_Anonymous(final String login_url)
    {
        Log.i("RequestProxy", "Register Anonymous");
        StringRequest postRequest = new StringRequest(Request.Method.POST, login_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        if (response.equals(server_login_failed))
                        {
                            Log.i("Register Anonymous Resp", "Login rejected: " + response);
//                            _token = "Test_ohne_login";
//                            ((MainActivity)_context).UpdateDebugInfo(" F ");
//                            Toast.makeText(RequestProxy.this, "Login OK", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Log.i("Register Anonymous Resp", "Token received: " + response);
//                            Toast.makeText(RequestProxy.this, "Login FAILED", Toast.LENGTH_SHORT).show();
//                            try {
//                                JSONObject myJson = new JSONObject(response);
//                                // use myJson as needed, for example
//                                String name = myJson.optString("name");
//                                String token = myJson.optString("auth_token");
//                                // etc
//                                _token = token;
//                                ((MainActivity)_context).UpdateDebugInfo(" L ");
//                            }
//                            catch (Exception e)
//                            {
//
//                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.i("Reg Anon Error.Resp", error.toString());
//                        _token = "";
//                        ((MainActivity)_context).UpdateDebugInfo(" F ");
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("anonym", "1");
                return params;
            }
        };

        //set timeout to 1000ms and retries to 1
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, // 1000
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, // 1
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)); //1f

        mRequestQueue.add(postRequest);
    }
}