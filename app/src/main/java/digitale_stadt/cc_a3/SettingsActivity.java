package digitale_stadt.cc_a3;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Created by Anne on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (z.B. W-Lan Einstellungen über Shared Preferences)
 */

public class SettingsActivity extends Activity{

    private Switch wlan_switch;

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings);

        wlan_switch = (Switch) findViewById(R.id.switch_wlan);

        //set the switch to off
        wlan_switch.setChecked(false);

        //attach listener to check for changes in state
        wlan_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO: Wert in Shared Prefs ändern
            }
        });
    }
}
