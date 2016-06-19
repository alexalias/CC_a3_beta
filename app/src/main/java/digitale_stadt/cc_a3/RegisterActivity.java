package digitale_stadt.cc_a3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Registrierungs-Activity. Neues Benutzerkonto wird angelegt.
 *
 * Created by Anne Lorenz on 15.06.2016.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText username;
    private EditText useremail;
    private EditText userpassword;
    private EditText userpassword2;


    private Button button_register;

    // Shared Preferences um Einstellungen zu speichern
    private SharedPreferences sharedPrefs;

    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setContentView(R.layout.activity_register);

        //initialize elements
        initializeComponents();

        //attach listeners to check for changes in state
        attachListeners();
    }

    private void initializeComponents() {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        username = (EditText) findViewById(R.id.edit_username);
        useremail = (EditText) findViewById(R.id.edit_userEmail);
        userpassword = (EditText) findViewById(R.id.edit_userPassword);
        userpassword2 = (EditText) findViewById(R.id.edit_userPassword2);

        button_register = (Button) findViewById(R.id.register);

        //setzt den Titel der Activity
        setTitle("Registrieren");

        setContents();
    }

    private void attachListeners() {

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        useremail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        userpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        userpassword2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                sharedPrefs.edit().putString("username", s.toString()).apply();
            }
        });

        useremail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefs.edit().putString("useremail", s.toString()).apply();
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
                sharedPrefs.edit().putString("userpassword", s.toString()).apply();
            }
        });

        userpassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                sharedPrefs.edit().putString("userpassword2", s.toString()).apply();
            }
        });

        //Register Parameters: URL, username, password, email
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((username.getText().toString().length() == 0) ||
                        (userpassword.getText().toString().length() == 0) ||
                        (useremail.getText().toString().length() == 0)) {
                    Toast.makeText(getApplicationContext(),
                            "Es fehlen wichtige Angaben", Toast.LENGTH_LONG).show();
                } else if (!passwordsAreIdentical()) {
                    Toast.makeText(getApplicationContext(),
                            "Passwörter stimmen nicht überein", Toast.LENGTH_LONG).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(useremail.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(),
                            "Bitte geben Sie eine gültige Emailadresse an", Toast.LENGTH_LONG).show();
                } else
                {
                    RequestManager.getInstance().doRequest().Register(
                            sharedPrefs.getString("username", ""),
                            sharedPrefs.getString("userpassword", ""),
                            sharedPrefs.getString("useremail", ""));

                    Toast.makeText(getApplicationContext(),
                            "Sie erhalten in Kürze eine Email mit Ihren Login-Daten", Toast.LENGTH_LONG).show();

                    //close activity
                    finish();
                }
            }
        });
    }

    private void setContents(){
        if (!sharedPrefs.getBoolean("anonymous", false)) {
            username.setText(sharedPrefs.getString("username", ""));
            useremail.setText(sharedPrefs.getString("useremail", ""));
            userpassword.setText(sharedPrefs.getString("userpassword", ""));
            userpassword2.setText(sharedPrefs.getString("userpassword2", ""));
        }
    }

    private boolean passwordsAreIdentical() {
        return (sharedPrefs.getString("userpassword", "").equals(sharedPrefs.getString("userpassword2", "")));
    }
}
