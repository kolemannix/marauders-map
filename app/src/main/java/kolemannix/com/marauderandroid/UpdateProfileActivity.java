package kolemannix.com.marauderandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
import android.widget.Spinner;

import org.json.JSONException;

import java.io.IOException;

/**
 * A login screen that offers login via username & email.
 */
public class UpdateProfileActivity extends Activity implements AdapterView.OnItemSelectedListener {

    // UI references.
    private TextView mEmailView;
    private EditText mUsernameView;
    private EditText mCustomizePassphrase;
    private SharedPreferences mSharedPref;
    private ImageView mIconView;
    private Spinner mIconSpinner;
    private final int[] ICONS = {R.drawable.hallows, R.drawable.wolf, R.drawable.stag, R.drawable.mouse_64};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_profile);

        // Set up the icon selector

        mIconView = (ImageView) findViewById(R.id.icon_view);
        mIconSpinner = (Spinner) findViewById(R.id.icon_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.icon_names, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mIconSpinner.setAdapter(adapter);
        mIconSpinner.setOnItemSelectedListener(this);

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_view);
        mEmailView = (TextView) findViewById(R.id.email_view);
        mCustomizePassphrase = (EditText) findViewById(R.id.customize_passphrase);

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
        mSharedPref = this.getSharedPreferences(
                getString(R.string.login_preferences_file_key), Context.MODE_PRIVATE);
        String defaultUsername = getString(R.string.def_username);
        String defaultEmail = getString(R.string.def_email);
        String defaultPassphrase = getString(R.string.def_passphrase);

        String storedUsername = mSharedPref.getString(getString(R.string.stored_username), defaultUsername);
        String storedEmail = mSharedPref.getString(getString(R.string.stored_email), defaultEmail);
        String storedPassphrase = mSharedPref.getString(getString(R.string.stored_custom_password), defaultPassphrase);

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

        if (storedEmail.contentEquals(defaultPassphrase)) {
            mCustomizePassphrase.setHint("Unlock");
        } else {
            mCustomizePassphrase.setText(storedPassphrase);
        }

        mIconSpinner.setSelection(mSharedPref.getInt(getString(R.string.stored_icon_id), 0));
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
        String passphrase = mCustomizePassphrase.getText().toString().trim();

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
        } else if (TextUtils.isEmpty(passphrase)) {
            mCustomizePassphrase.setError(getString(R.string.error_field_required));
            focusView = mCustomizePassphrase;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            int iconID = mIconSpinner.getSelectedItemPosition();

            SharedPreferences.Editor editor = mSharedPref.edit();
            editor.putString(getString(R.string.stored_username), username);
            editor.putString(getString(R.string.stored_email), email);
            editor.putInt(getString(R.string.stored_icon_id), iconID);
            editor.putString(getString(R.string.stored_custom_password), passphrase);
            editor.apply();

            MarauderProfile prof = new MarauderProfile(email, username, iconID);
            continueToMap(prof);
        }
    }

    private void continueToMap(MarauderProfile profile) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        resultIntent.putExtra("profile", profile.toStringArray());
        finish();
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }


    public void clickedClearProfile(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfileActivity.this);
        builder.setMessage("Are you sure you want to clear this profile? All profile information will be lost.");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                clearProfile();
                dialog.cancel();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog clearAlert = builder.create();
        clearAlert.show();
    }



    public void clearProfile() {
        String defaultUsername = getString(R.string.def_username);
        String defaultEmail = getString(R.string.def_email);
        String defaultPassphrase = getString(R.string.def_passphrase);

        String storedUsername = mSharedPref.getString(getString(R.string.stored_username), defaultUsername);
        String storedEmail = mSharedPref.getString(getString(R.string.stored_email), defaultEmail);
        int storedIcon = mSharedPref.getInt(getString(R.string.stored_icon_id), 0);
        MarauderProfile prof = new MarauderProfile(storedEmail, storedUsername, storedIcon);


        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(getString(R.string.stored_username), defaultUsername);
        editor.putString(getString(R.string.stored_email), defaultEmail);
        editor.putInt(getString(R.string.stored_icon_id), -1);
        editor.putString(getString(R.string.stored_custom_password), defaultPassphrase);
        editor.apply();


        Intent resultIntent = new Intent();
        setResult(MapActivity.RESET_PROFILE, resultIntent);
        resultIntent.putExtra("profile", prof.toStringArray());
        finish();
    }

}
