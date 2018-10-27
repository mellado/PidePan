package es.pablomellado.pandealfacar.register;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import es.pablomellado.pandealfacar.R;

/**
 * A register screen that allows to write your verify your phone number .
 */
public class RegisterActivity extends AppCompatActivity {


    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;
    // TODO: Set the final username, password and sender before release
    private final String SMS_SERVICE_LOGIN = "";
    private final String SMS_SERVICE_PASSWORD = "";
    private final String SMS_SERVICE_SENDER = "";



    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private EditText mPhoneNumberView;
    private View mProgressView;
    private TextView mProgressMessage;
    private View mLoginFormView;

    public static String mPhoneNumber;
    private String mRandomId;
    public static boolean wasMyOwnNumber=false;
    public static boolean workDone=false;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the register form.

        mPhoneNumberView = (EditText) findViewById(R.id.phone_number);
        mPhoneNumberView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.phone_number || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mPhoneRegisterButton = (Button) findViewById(R.id.phone_register_button);
        mPhoneRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mProgressMessage = (TextView)findViewById(R.id.login_message);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.verify_your_number_title);
            setSupportActionBar(toolbar);
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mPhoneNumberView.setError(null);

        // Store values at the time of the login attempt.
        String phoneNumber = mPhoneNumberView.getText().toString();



        boolean cancel = false;
        View focusView = null;

        phoneNumber = cleanPhoneString(phoneNumber);
        mPhoneNumber = phoneNumber;

        // Check for a valid email address.
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        } else if (!isPhoneLengthValid(phoneNumber)){
            mPhoneNumberView.setError(getString(R.string.error_phone_length));
            focusView = mPhoneNumberView;
            cancel = true;
        } else if (!isPhoneValid(phoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_invalid_phone));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECEIVE_SMS},
                    MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
            showProgress(true);

            saveUniqueId();

            mAuthTask = new UserRegisterTask(this);
            mAuthTask.execute((Void) null);
        }
    }

    private void saveUniqueId(){

        mRandomId =UUID.randomUUID().toString();
        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.random_id), mRandomId);
        editor.putString(getString(R.string.my_phone_number), mPhoneNumber);
        editor.commit();
    }

    private String cleanPhoneString (String phone){
        phone = phone.replace(" ","");
        phone = phone.replace(".", "");
        phone = phone.replace("(", "");
        phone = phone.replace(")", "");

        return phone;

    }

    private boolean isPhoneValid(String phone) {
        if (!phone.matches("\\d+")){
            return false;
        }

        if (phone.charAt(0)=='6' || phone.charAt(0)=='7'){
            return true;
        }
        return false;
    }

    private boolean isPhoneLengthValid(String phone){
        if (phone.length()==9){
            return true;
        }
        return false;
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

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
        }
        mProgressMessage.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final int SECONDS_WAITING_SMS = 90;
        private final AppCompatActivity mThisActivity;

        UserRegisterTask(AppCompatActivity activity) {
            mThisActivity = activity;
        }

        private boolean sendSMS(){
            String result = null;
            int resCode;
            InputStream in;

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };

            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (GeneralSecurityException e) {
            }

            try {
                String smsText = Uri.encode(String.format("%s %s", getString(R.string.app_name),
                        mRandomId));
                String fullUrl = String.format("https://your.server.com/GatewayListener?" +
                                "login=%s&pass=%s&"+
                                "command=CREATEPUSHEXPRESS&"+
                                "name=%s&"+
                                "sender=%s&"+
                                "text=%s&"+
                                "date=2017-01-01&time=00:00:00&"+
                                "type=sms&method=premium&"+
                                "contacts=%s",
                        Uri.encode(SMS_SERVICE_LOGIN), Uri.encode(SMS_SERVICE_PASSWORD),
                        mPhoneNumber, Uri.encode(SMS_SERVICE_SENDER),
                        smsText, mPhoneNumber);
                URL url = new URL(fullUrl);
                URLConnection urlConn = url.openConnection();

                HttpsURLConnection httpsConn = (HttpsURLConnection) urlConn;
                httpsConn.setAllowUserInteraction(false);
                httpsConn.setInstanceFollowRedirects(true);
                httpsConn.setRequestMethod("GET");

                httpsConn.connect();
                resCode = httpsConn.getResponseCode();

                if (resCode == HttpURLConnection.HTTP_OK) {
                    in = httpsConn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            in, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    in.close();
                    result = sb.toString();
                    if (result.substring(0,2).equals("KO")){
                        return false;
                    }
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!sendSMS()) {
                return false;
            }
            SharedPreferences sharedPref = mThisActivity.getSharedPreferences(
                    mThisActivity.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            for (int i=0; i <SECONDS_WAITING_SMS; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return false;
                }
                // Checking if the SMS was received and the client verfied
                if (sharedPref.getBoolean(mThisActivity.getString(R.string.phone_is_confirmed), false)) {
                    // Got SMS
                    return true;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                setResult(RESULT_OK);
                finish();
            } else {
                new MaterialDialog.Builder(mThisActivity)
                        .content(R.string.error_incorrect_phone)
                        .positiveText(android.R.string.ok)
                        .show();
                mPhoneNumberView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
            new MaterialDialog.Builder(mThisActivity)
                    .content(R.string.error_incorrect_phone)
                    .positiveText(android.R.string.ok)
                    .show();
            mPhoneNumberView.requestFocus();
        }
    }
}

