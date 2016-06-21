package digitale_stadt.cc_a3;

import android.content.Context;

/**
 * Created by lorcan on 25.05.2016.
 */
public class DBManager {

    private static DBManager instance;
    private DBHelper mDBHelper;

    private DBManager(Context context) {
        mDBHelper = new DBHelper(context);
    }
    public DBHelper doRequest() {
        return mDBHelper;
    }

    // This method should be called first to do singleton initialization
    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context);
        }
        return instance;
    }

    public static synchronized DBManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DBManager.class.getSimpleName() +
                    " is not initialized, call getInstance(..) method first.");
        }
        return instance;
    }
}
