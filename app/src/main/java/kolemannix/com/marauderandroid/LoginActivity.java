package kolemannix.com.marauderandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via username & email.
 */
public class LoginActivity extends Activity implements AdapterView.OnItemSelectedListener {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mUsernameView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences sharedPref;
    private ImageView mIconView;
    private Spinner mIconSpinner;
    private final int[] ICONS = {R.drawable.hallows, R.drawable.wolf, R.drawable.stag};

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

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void setupSharedPreferences() {
        sharedPref = this.getSharedPreferences(
                getString(R.string.login_preferences_file_key), Context.MODE_PRIVATE);

        String recent_username = sharedPref.getString(getString(R.string.most_recent_username), "Nickname");
        String recent_email = sharedPref.getString(getString(R.string.most_recent_email), "Email");

        if (recent_username.contentEquals("Nickname")) {
            mUsernameView.setHint(recent_username);
        } else {
            mUsernameView.setText(recent_username);
        }

        if (recent_email.contentEquals("Email")) {
            mEmailView.setHint(recent_email);
        } else {
            mEmailView.setText(recent_email);
        }

        mIconSpinner.setSelection(sharedPref.getInt(getString(R.string.most_recent_icon_id), 0));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)

        mIconView.setImageResource(ICONS[pos]);
//
//        if(mIconSelection.equals("Wolf")) {
//            mIconView.setImageResource(R.drawable.wolf);
//        } else if (mIconSelection.equals("Stag")) {
//            mIconView.setImageResource(R.drawable.stag);
//        } else {
//            mIconView.setImageResource(R.drawable.default_prof);
//        }

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
        if (mAuthTask != null) {
            return;
        }

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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            String iconIDAsString = Integer.toString(mIconSpinner.getSelectedItemPosition());

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.most_recent_username), username);
            editor.putString(getString(R.string.most_recent_email), email);
            editor.putInt(getString(R.string.most_recent_icon_id), mIconSpinner.getSelectedItemPosition());
            editor.apply();

            mAuthTask = new UserLoginTask(username, email, iconIDAsString);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private String mUsername;
        private String mIconId;

        UserLoginTask(String username, String email, String iconId) {
            mUsername = username;
            mEmail = email;
            mIconId = iconId;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt login against a network service.
            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                String[] profile = {mUsername, mEmail, mIconId};
                intent.putExtra("profile", profile);
                finish();
                startActivity(intent);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

