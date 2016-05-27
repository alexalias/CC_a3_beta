package digitale_stadt.cc_a3;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by lorcan on 25.05.2016.
 */
public class RequestProxy {

    private RequestQueue mRequestQueue;

    // package access constructor
    RequestProxy(Context context) {
        mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void login() {
        // login request
    }

    public void weather() {
        // weather request
    }
}