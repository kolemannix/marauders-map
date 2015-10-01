package kolemannix.com.marauderandroid;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
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
    private final int REQ_CODE_SPEECH_INPUT = 100;

    static final String INSULT_NUM = "insultIndex";

    TextView mMessageView;
    Button mInsultButton;
    Button mUnlockButton;
    int insultIndex;
    int[] insult_ids = {R.string.insult_1, R.string.insult_2, R.string.insult_3, R.string.insult_4};

    List<String> speechInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mMessageView = (TextView)findViewById(R.id.splash_message_text);
        mInsultButton = (Button)findViewById(R.id.insult_button);
        mUnlockButton = (Button)findViewById(R.id.unlock_button);

        if(savedInstanceState != null) {
            insultIndex = savedInstanceState.getInt(INSULT_NUM);
        }

        String insult = getString(insult_ids[insultIndex]);
        mMessageView.setText(insult);


        // Launch asynchronous listener process
        locations = Service.getLocations();
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.action_sign_in));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.action_sign_in),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    speechInput = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                }
                break;
            }

        }
    }

    public void insult(View view) {
        insultIndex = (insultIndex + 1) % 4;
        String insult = getString(insult_ids[insultIndex]);
        mMessageView.setText(insult);
    }

    public void unlock(View view) {
        // Check the local keystore
        MarauderProfile profile = checkStorage();
        if (profile != null) {
            // Launch the map activity
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);

        } else {
            // Launch registration activity
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private MarauderProfile checkStorage() {
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(INSULT_NUM, insultIndex);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

}
