package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by Anne on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (z.B. W-Lan Einstellungen über Shared Preferences)
 */

public class SettingsActivity extends Activity{

    private Switch wlan_switch;

    // Shared Preferences um Einstellungen zu speichern
    SharedPreferences sharedPrefs;

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        wlan_switch = (Switch) findViewById(R.id.switch_wlan);

        //initialize switch
        wlan_switch.setChecked(sharedPrefs.getBoolean("wlan_upload", false));

        //attach listener to check for changes in state
        wlan_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("wlan_upload", isChecked);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Einstellung wurde gespeichert", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
