package digitale_stadt.cc_a3;


import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by Anne Lorenz on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (gespeichert über Shared Preferences)
 */

public class SettingsActivity extends PreferenceActivity {

    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        addPreferencesFromResource(R.layout.activity_settings);
    }
}
