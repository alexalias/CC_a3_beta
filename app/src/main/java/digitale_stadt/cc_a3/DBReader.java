package digitale_stadt.cc_a3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Ralf Engelken on 03.05.16.
 */

//ToDo: This sevice shall run in a seperate thread, look up the DB, send unsent waypoints to the server and mark them as sent in the DB
//uses DBHelper and Sender
public class DBReader extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
