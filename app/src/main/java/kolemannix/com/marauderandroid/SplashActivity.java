package kolemannix.com.marauderandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class SplashActivity extends Activity {

    // For now, we assume stored creds = good creds
    Map<MarauderProfile, Location> locations = null;

    static final String INSULT_NUM = "insultIndex";
    static final String TYPING_ENABLED = "typing_enabled";

    TextView mMessageView;
    TextView mTitleView;
    EditText mEnterPassphrase;
    Button mEnterButton;

    int insultIndex;
    int[] insult_ids = {R.string.insult_1, R.string.insult_2, R.string.insult_3, R.string.insult_4};

    private Intent mRecognizerIntent;
    private SharedPreferences mSharedPref;

    private boolean shouldDisplayTypingOption = false;
    private final int RESULT_SPEECH = 1337;
    private final String TITLE_MESSAGE = "titleMessage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        mMessageView = (TextView)findViewById(R.id.splash_message_text);
        mTitleView = (TextView)findViewById(R.id.title_view);
        mEnterPassphrase = (EditText)findViewById(R.id.enter_passphrase_view);
        mEnterButton = (Button) findViewById(R.id.enter_passphrase_button);

//        mEnterPassphrase.setImeActionLabel("Go", KeyEvent.KEYCODE_ENTER);
        mEnterPassphrase.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i("Hi mom!", Integer.toString(actionId));
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    checkInput(v.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });

        mSharedPref = this.getSharedPreferences(
                getString(R.string.login_preferences_file_key), Context.MODE_PRIVATE);

        if(savedInstanceState != null) {
            insultIndex = savedInstanceState.getInt(INSULT_NUM, -1);
            if (insultIndex != -1) {
                String insult = getString(insult_ids[insultIndex]);
                mMessageView.setText(insult);
                mTitleView.setText("Access Denied");
            }
            shouldDisplayTypingOption = savedInstanceState.getBoolean(TYPING_ENABLED);
            if (shouldDisplayTypingOption) {
                mEnterPassphrase.setVisibility(View.VISIBLE);
                mEnterButton.setVisibility(View.VISIBLE);
            }
        } else {
            insultIndex = -1;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SPEECH) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> speechInput = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                checkInput(speechInput.get(0));
            }
        }
    }

    private void checkInput(String s) {
        String customPassword = mSharedPref.getString(getString(R.string.stored_custom_password),
                "unlock").toLowerCase();
        Log.i("Said vs Password", s + " +vs+ " + customPassword);

        if (s.trim().toLowerCase().contentEquals("unlock")
                || s.toLowerCase().contentEquals("i solemnly swear i'm up to no good")
                || s.toLowerCase().contentEquals("i solemnly swear i am up to no good")
                || s.toLowerCase().contentEquals("i solemnly swear that i am up to no good")
                || s.toLowerCase().contentEquals("i solemnly swear that i'm up to no good")
                || s.toLowerCase().contentEquals(customPassword)) {
            unlock();
        } else {
            insult();
        }
    }

    public void insult() {
        insultIndex = (insultIndex + 1) % 4;
        String insult = getString(insult_ids[insultIndex]);
        mMessageView.setText(insult);
        mTitleView.setText("Access Denied");
    }

    public void resetToStartState() {
        insultIndex = -1;
        mMessageView.setText(getString(R.string.greeting));
        mTitleView.setText(getString(R.string.app_title));
        mEnterPassphrase.setText("  ");
        mEnterButton.setVisibility(View.INVISIBLE);
        mEnterPassphrase.setVisibility(View.INVISIBLE);
    }

    public void unlock() {
        // Check the local profile store
        MarauderProfile profile = checkStorage();
        if (profile != null) {
            // Launch the map activity
            Toast.makeText(this, "Welcome, Mischief Maker", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("profile", profile.toStringArray());
            resetToStartState();
            startActivity(intent);
        } else {
            // Launch registration activity
            Intent intent = new Intent(this, LoginActivity.class);
            resetToStartState();
            startActivity(intent);
        }
    }

    public void listen(View view) {
        startActivityForResult(mRecognizerIntent, RESULT_SPEECH);
    }

    public void typePassphrase(View view) {
        mEnterPassphrase.setVisibility(View.VISIBLE);
        mEnterButton.setVisibility(View.VISIBLE);
        mEnterPassphrase.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEnterPassphrase, InputMethodManager.SHOW_IMPLICIT);
        shouldDisplayTypingOption = true;
    }

    public void enterPassphrase(View view) {
        String userInput = mEnterPassphrase.getText().toString().toLowerCase();
        checkInput(userInput);
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
        savedInstanceState.putString(TITLE_MESSAGE, mTitleView.getText().toString());
        savedInstanceState.putBoolean(TYPING_ENABLED, shouldDisplayTypingOption);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
}
