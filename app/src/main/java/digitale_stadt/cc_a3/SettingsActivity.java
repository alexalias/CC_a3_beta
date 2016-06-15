package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Anne Lorenz on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (gespeichert über Shared Preferences)
 */

public class SettingsActivity extends Activity{

    private CheckBox checkBox_wlan;

    // Shared Preferences um Einstellungen zu speichern
    private SharedPreferences sharedPrefs;

    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_settings);

        //initialize elements
        initializeComponents();

        //attach listeners to check for changes in state
        attachListeners();
    }

    private void initializeComponents() {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        checkBox_wlan = (CheckBox) findViewById(R.id.checkBox_wlan);
        checkBox_wlan.setChecked(sharedPrefs.getBoolean("wlan_upload", false));
    }

    private void attachListeners() {

        checkBox_wlan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (compoundButton.isChecked()) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("wlan_upload", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putBoolean("wlan_upload", false);
                    editor.apply();
                }
            }
        });
    }

    public Boolean getWlanOption() {
        return sharedPrefs.getBoolean("wlan_upload", false);
    }
}
