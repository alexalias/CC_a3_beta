package digitale_stadt.cc_a3;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Registrierungs-Activity. Neues Benutzerkonto wird angelegt.
 *
 * //ToDo: Korrekte Eingaben erzwingen (Email usw.)
 *
 * Created by Anne Lorenz on 15.06.2016.
 */
public class RegisterActivity extends Activity {

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
        userpassword2 = (EditText) findViewById(R.id.edit_userPassword);

        button_register = (Button) findViewById(R.id.register);
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
                sharedPrefs.edit().putString("username", s.toString()).commit();
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
                sharedPrefs.edit().putString("useremail", s.toString()).commit();
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

        userpassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!sharedPrefs.getString("userpassword", "").equals(userpassword2.getText().toString())){
                    Toast.makeText(RegisterActivity.this, "Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Register Parameters: URL, username, password, email
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestManager.getInstance().doRequest().Register(
                        sharedPrefs.getString("username", ""),
                        sharedPrefs.getString("userpassword", ""),
                        sharedPrefs.getString("useremail", ""));
                //close activity
                finish();
            }
        });
    }
}
