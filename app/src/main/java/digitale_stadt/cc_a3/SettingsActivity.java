package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

/**
 * Created by Anne Lorenz on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (gespeichert über Shared Preferences)
 */

public class SettingsActivity extends Activity{

    private Switch wlan_switch;
    private EditText username;
    private EditText userpassword;

    // Shared Preferences um Einstellungen zu speichern
    SharedPreferences sharedPrefs;

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
        wlan_switch = (Switch) findViewById(R.id.switch_wlan);
        username = (EditText) findViewById(R.id.edit_username);
        userpassword = (EditText) findViewById(R.id.edit_userPassword);

        wlan_switch.setChecked(sharedPrefs.getBoolean("wlan_upload", false));
        username.setText(sharedPrefs.getString("username", "Bitte Benutzernamen angeben"));
        userpassword.setText(sharedPrefs.getString("userpassword", "Bitte Passwort angeben"));
    }

    private void attachListeners() {

        wlan_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("wlan_upload", isChecked);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Einstellung wurde gespeichert", Toast.LENGTH_SHORT).show();
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefs.edit().putString("username", s.toString()).commit();
            }
        });

        userpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefs.edit().putString("userpassword", s.toString()).commit();
            }
        });
    }

    public String getUserName() {
        return sharedPrefs.getString("username", "");
    }

    public String getUserPassword() {
        return sharedPrefs.getString("userpassword", "");
    }
}
