package digitale_stadt.cc_a3;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Anne Lorenz on 16.06.2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
