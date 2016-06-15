package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * LoginActivity. Wird von MainActivity aus beim erstmaligen Start der App gestartet.
 *
 * Created by Anne Lorenz on 15.06.2016.
 */

public class LoginActivity extends Activity {

    private EditText username;
    private EditText userpassword;

    private Button button_login;
    private Button button_register;
    private Button button_anonymous;

    // Shared Preferences um Einstellungen zu speichern
    private SharedPreferences sharedPrefs;

    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_login);

        //initialize elements
        initializeComponents();

        //attach listeners to check for changes in state
        attachListeners();
    }

    private void initializeComponents() {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        username = (EditText) findViewById(R.id.edit_username);
        userpassword = (EditText) findViewById(R.id.edit_userPassword);

        button_login = (Button) findViewById(R.id.login);
        button_register = (Button) findViewById(R.id.register);
        button_anonymous = (Button) findViewById(R.id.anonymous);

        username.setText(sharedPrefs.getString("username", "Bitte Benutzernamen angeben"));
        userpassword.setText(sharedPrefs.getString("userpassword", "Bitte Passwort angeben"));
    }

    private void attachListeners() {

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

        //Login Parameters: URL, username, password
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestManager.getInstance().doRequest().Login("https://api.cyc.jmakro.de:4040/get_auth_token.php",
                        sharedPrefs.getString("username", ""), sharedPrefs.getString("userpassword", ""));
            }
        });

        //RegisterActivity wird gestartet
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        //Register Parameters: URL
        button_anonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestManager.getInstance().doRequest().Register_Anonymous("https://api.cyc.jmakro.de:4040/register_user.php");
            }
        });
    }
}
