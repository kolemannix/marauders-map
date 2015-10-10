package kolemannix.com.marauderandroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SplashActivity extends Activity {

    // For now, we assume stored creds = good creds
    Map<MarauderProfile, Location> locations = null;

    static final String INSULT_NUM = "insultIndex";

    TextView mMessageView;
    Button mInsultButton;
    Button mUnlockButton;
    int insultIndex;
    int[] insult_ids = {R.string.insult_1, R.string.insult_2, R.string.insult_3, R.string.insult_4};

    private Intent mRecognizerIntent;
    private final int RESULT_SPEECH = 1337;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        mMessageView = (TextView)findViewById(R.id.splash_message_text);

        if(savedInstanceState != null) {
            insultIndex = savedInstanceState.getInt(INSULT_NUM);
            String insult = getString(insult_ids[insultIndex]);
            mMessageView.setText(insult);
        }

        // Launch asynchronous listener process
        locations = Service.getLocations();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SPEECH) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> speechInput = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                checkSpeechInput(speechInput.get(0));
            }
        }
    }

    private void checkSpeechInput(String s) {
        Log.i("Said", s);
        if (s.toLowerCase().contentEquals("unlock")
                || s.toLowerCase().contentEquals("i solemnly swear i'm up to no good")
                || s.toLowerCase().contentEquals("i solemnly swear i am up to no good")) {
            unlock();
        } else {
            insult();
        }
    }

    public void insult() {
        insultIndex = (insultIndex + 1) % 4;
        String insult = getString(insult_ids[insultIndex]);
        mMessageView.setText(insult);
    }

    public void unlock() {
        // Check the local profile store
        MarauderProfile profile = checkStorage();
        if (profile != null) {
            // Launch the map activity
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("profile", profile.toStringArray());
            startActivity(intent);

        } else {
            // Launch registration activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void listen(View view) {
        startActivityForResult(mRecognizerIntent, RESULT_SPEECH);
    }

    private MarauderProfile checkStorage() {
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.login_preferences_file_key), Context.MODE_PRIVATE);
        String defaultUsername = getString(R.string.def_username);
        String defaultEmail = getString(R.string.def_email);

        String stored_username = sharedPref.getString(getString(R.string.stored_username), defaultUsername);
        String stored_email = sharedPref.getString(getString(R.string.stored_email), defaultEmail);
        int stored_icon = sharedPref.getInt(getString(R.string.stored_icon_id), -1);

        if (stored_username.contentEquals(defaultUsername)
                || stored_email.contentEquals(defaultEmail)
                || stored_icon == -1) {
            return null;
        }
        return new MarauderProfile(stored_email, stored_username, stored_icon);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(INSULT_NUM, insultIndex);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
