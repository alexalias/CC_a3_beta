package digitale_stadt.cc_a3;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Anne Lorenz on 23.05.2016.
 *
 * Diese Klasse beinhaltet die Settings (gespeichert über Shared Preferences)
 */

public class SettingsActivity extends AppCompatActivity{

    public void onCreate(Bundle savedInstance) {

        super.onCreate(savedInstance);
        setTitle("Einstellungen");

        //display the fragment as main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
