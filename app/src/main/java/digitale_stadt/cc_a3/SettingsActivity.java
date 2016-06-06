package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
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
    private Button button_reset;

    // Shared Preferences um Einstellungen zu speichern
    private SharedPreferences sharedPrefs;

    private DBHelper dbHelper;

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
        button_reset = (Button) findViewById(R.id.button_reset);

        wlan_switch.setChecked(sharedPrefs.getBoolean("wlan_upload", false));
        username.setText(sharedPrefs.getString("username", "Bitte Benutzernamen angeben"));
        userpassword.setText(sharedPrefs.getString("userpassword", "Bitte Passwort angeben"));

        dbHelper = new DBHelper(this);
    }

    private void attachListeners() {

        wlan_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean("wlan_upload", isChecked);
                editor.apply();
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username.setText("");
            }
        });

        userpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userpassword.setText("");
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

        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteDatabaseAsync(new DBHelper.DatabaseHandler<Void>() {
                    @Override
                    public void onComplete(boolean success, Void result) {
                        //TODO: ?
                    }
                });
                sharedPrefs.edit().clear().commit();
                wlan_switch.setChecked(sharedPrefs.getBoolean("wlan_upload", false));
                username.setText(sharedPrefs.getString("username", "Bitte Benutzernamen angeben"));
                userpassword.setText(sharedPrefs.getString("userpassword", "Bitte Passwort angeben"));
                Toast.makeText(SettingsActivity.this, "Einstellungen wurden gelöscht", Toast.LENGTH_SHORT).show();
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
