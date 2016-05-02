package digitale_stadt.cc_a3;


import android.app.Application;
import android.content.Context;

/**
 * Created by alexutza_a on 29.04.2016.
 */
public class AppContextProvider extends Application {

    public static Context appContext;

    @Override
    public void onCreate(){
        super.onCreate();
        appContext= getApplicationContext();
    }


    public static Context getAppContext(){
        return appContext;
    }
}
