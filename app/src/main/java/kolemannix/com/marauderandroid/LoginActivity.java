package kolemannix.com.marauderandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

/**
 * A login screen that offers login via username & email.
 */
public class LoginActivity extends Activity implements AdapterView.OnItemSelectedListener {

    // UI references.
    private EditText mEmailView;
    private EditText mUsernameView;
    private SharedPreferences sharedPref;
    private ImageView mIconView;
    private Spinner mIconSpinner;
    private final int[] ICONS = {R.drawable.hallows, R.drawable.wolf, R.drawable.stag, R.drawable.mouse_64};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the icon selector

        mIconView = (ImageView) findViewById(R.id.icon_view);
        mIconSpinner = (Spinner)findViewById(R.id.icon_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.icon_names, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mIconSpinner.setAdapter(adapter);
        mIconSpinner.setOnItemSelectedListener(this);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_view);
        mEmailView = (EditText) findViewById(R.id.email_view);

        setupSharedPreferences();

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

    }

    private void setupSharedPreferences() {
        sharedPref = this.getSharedPreferences(
                getString(R.string.login_preferences_file_key), Context.MODE_PRIVATE);
        String defaultUsername = getString(R.string.def_username);
        String defaultEmail = getString(R.string.def_email);

        String storedUsername = sharedPref.getString(getString(R.string.stored_username), defaultUsername);
        String storedEmail = sharedPref.getString(getString(R.string.stored_email), defaultEmail);

        if (storedUsername.contentEquals(defaultUsername)) {
            mUsernameView.setHint("Nickname");
        } else {
            mUsernameView.setText(storedUsername);
        }

        if (storedEmail.contentEquals(defaultEmail)) {
            mEmailView.setHint("Email");
        } else {
            mEmailView.setText(storedEmail);
        }

        mIconSpinner.setSelection(sharedPref.getInt(getString(R.string.stored_icon_id), 0));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mIconView.setImageResource(ICONS[pos]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        // Store values at the time of the login attempt.

        String email = mEmailView.getText().toString();
        String username = mUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            int iconID = mIconSpinner.getSelectedItemPosition();

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.stored_username), username);
            editor.putString(getString(R.string.stored_email), email);
            editor.putInt(getString(R.string.stored_icon_id), iconID);
            editor.apply();

            MarauderProfile prof = new MarauderProfile(email, username, iconID);
            continueToMap(prof);
        }
    }

    private void continueToMap(MarauderProfile profile) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("profile",  profile.toStringArray());
        startActivity(intent);
        finish();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

}

