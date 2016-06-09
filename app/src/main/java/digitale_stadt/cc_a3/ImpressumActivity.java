package digitale_stadt.cc_a3;

import android.app.Activity;
import android.os.Bundle;

/**
 * Kleinste Klasse der Welt. :-)
 * Enthält nur einen kurzen Impressumtext.
 *
 * Created by Anne Lorenz on 08.06.2016.
 */
public class ImpressumActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_impressum);
    }

}
