package digitale_stadt.cc_a3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * LoginActivity. Wird von MainActivity aus beim erstmaligen Start der App gestartet.
 *
 * Created by Anne Lorenz on 15.06.2016.
 */

public class LoginActivity extends AppCompatActivity {

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

        // setze vorhandene Logindaten, falls nicht anonym
        if (!sharedPrefs.getBoolean("anonymous", false)) {
            username.setText(sharedPrefs.getString("username", ""));
            userpassword.setText(sharedPrefs.getString("userpassword", ""));
        }
        setTitle("Login");
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
                sharedPrefs.edit().putBoolean("anonymous", false).commit();
                RequestManager.getInstance().doRequest().Login(
                        sharedPrefs.getString("username", ""),
                        sharedPrefs.getString("userpassword", ""));
                //close activity
                finish();
            }
        });

        //RegisterActivity wird gestartet
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                //close activity
                finish();
            }
        });

        //Register Parameters: URL
        button_anonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestManager.getInstance().doRequest().Register_Anonymous();
                //close activity
                finish();
            }
        });
    }
}
