package digitale_stadt.cc_a3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Hier steht ein kurzer Impressumtext.
 *
 * Created by Anne Lorenz on 08.06.2016.
 */
public class ImpressumActivity extends Activity {

    private TextView text1;

    public void OnCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_impressum);
    }

}
