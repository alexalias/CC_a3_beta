package digitale_stadt.cc_a3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Enthält kurzen Impressumtext.
 *
 * Created by Anne Lorenz on 08.06.2016.
 */
public class ImpressumActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_impressum);
        setTitle(R.string.title_impressumActivity);
    }

}
