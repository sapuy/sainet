package xyz.leosap.multiplace.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

import cz.msebera.android.httpclient.entity.StringEntity;
import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.common.Constants;
import xyz.leosap.multiplace.common.Functions;


public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {


    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView et_email;
    private EditText et_password, et_password_2, et_email_2;
    private View vw_progress;
    private View vw_container;
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private final FacebookCallback callback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(final LoginResult loginResult) {

            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(),
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {

                            try {
                                Log.d("LS FB", response.toString());
                                String email = object.getString("email");

                                JSONObject values = new JSONObject();
                                values.put("name", email);
                                values.put("pass", "");
                                values.put("mail", email);
                                values.put("init", email);
                                values.put("roles", "authenticated user");
                                values.put("status", "1");


                                String birthdate[] = object.getString("birthday").split("/");

                                JSONObject fb_data = new JSONObject();
                                fb_data.put("identifier", object.getString("id"));
                                fb_data.put("username", "test");
                                fb_data.put("displayName", object.getString("name"));
                                fb_data.put("firstName", object.getString("first_name"));
                                fb_data.put("lastName", object.getString("last_name"));
                                fb_data.put("gender", object.getString("gender"));
                                fb_data.put("language", object.getString("locale"));
                                fb_data.put("description", "descripcion");
                                fb_data.put("email", email);
                                fb_data.put("emailVerified", email);
                                fb_data.put("region", "BOG-CO");
                                fb_data.put("city", object.getJSONObject("location").get("name"));
                                fb_data.put("country", "Colombia");
                                fb_data.put("birthDay", birthdate[0]);
                                fb_data.put("birthMonth", birthdate[1]);
                                fb_data.put("birthYear", birthdate[2]);
                                fb_data.put("token", loginResult.getAccessToken());


                                values.put("data", fb_data);
                                register(values);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,first_name,last_name,email,birthday,location,gender,timezone,verified,languages,locale");
            request.setParameters(parameters);
            request.executeAsync();
            LoginManager.getInstance().logOut();


        }

        @Override
        public void onCancel() {
            // App code
        }

        @Override
        public void onError(FacebookException exception) {
            // App code
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        Functions.checkLogin(getApplicationContext());
        //
        et_email = (AutoCompleteTextView) findViewById(R.id.et_email);
        et_email_2 = (EditText) findViewById(R.id.et_email_confirm);
        et_password = (EditText) findViewById(R.id.et_password);
        et_password_2 = (EditText) findViewById(R.id.et_password_confirm);

        et_password_2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    (findViewById(R.id.bt_login)).performClick();
                    return true;
                }
                return false;
            }
        });

        populateAutoComplete();

        vw_container = findViewById(R.id.vw_container);
        vw_progress = findViewById(R.id.vw_progress);


        //FB Login
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("user_status", "email", "user_friends", "public_profile", "user_location", "user_birthday"));
        //Facebook SDK

        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, callback);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void populateAutoComplete() {
        if (!Functions.mayRequestContacts(getApplicationContext(), vw_container)) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.link_login:
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                break;

            case R.id.bt_login:
                // Reset errors.
                et_email.setError(null);
                et_email_2.setError(null);
                et_password_2.setError(null);
                et_password.setError(null);

                String email = et_email.getText().toString().trim().toLowerCase();
                String password = et_password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    et_email.setError(getString(R.string.error_field_required));
                    et_email.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(et_email_2.getText().toString().trim())) {
                    et_email_2.setError(getString(R.string.error_field_required));
                    et_email_2.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    et_password.setError(getString(R.string.error_field_required));
                    et_password.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(et_password_2.getText().toString().trim())) {
                    et_password_2.setError(getString(R.string.error_field_required));
                    et_password_2.requestFocus();
                    return;
                }

                if (!Functions.isEmailValid(email)) {
                    et_email.setError(getString(R.string.error_invalid_email));
                    et_email.requestFocus();
                    return;
                }

                if (!Functions.isEmailValid(et_email_2.getText().toString().trim())) {
                    et_email_2.setError(getString(R.string.error_invalid_email));
                    et_email_2.requestFocus();
                    return;
                }

                if (!et_email_2.getText().toString().equals(email)) {
                    et_email_2.setError(getString(R.string.error_missmatch_email));
                    et_email_2.requestFocus();
                    return;
                }

                if (!Functions.isPasswordValid(password)) {
                    et_password.setError(getString(R.string.error_invalid_password));
                    et_password.requestFocus();
                    return;
                }

                if (!Functions.isPasswordValid(et_password_2.toString().trim())) {
                    et_password_2.setError(getString(R.string.error_invalid_password));
                    et_password_2.requestFocus();
                    return;
                }

                if (!et_password_2.getText().toString().equalsIgnoreCase(password)) {
                    et_password_2.setError(getString(R.string.error_missmatch_password));
                    et_password_2.requestFocus();
                    return;
                }

                //Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);
                try {
                    JSONObject values = new JSONObject();
                    values.put("name", email);
                    values.put("pass", password);
                    values.put("mail", email);
                    values.put("init", email);
                    values.put("roles", "authenticated user");
                    values.put("status", "1");
                    register(values);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }


    }

    private void register(final JSONObject values) {
        try {


            AsyncHttpClient client = new AsyncHttpClient();

            client.addHeader("Accept", "application/json");
            client.addHeader("Content-Type", "application/json");
            //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
            client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
            Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);
            client.post(getApplicationContext(), Constants.URL_user, new StringEntity(values.toString()), "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);
                        Functions.getPreferences(getApplicationContext()).edit().putBoolean("logued", true).apply();
                        Functions.getPreferences(getApplicationContext()).edit().putString("uid", response.getString("id")).apply();
                        Functions.getPreferences(getApplicationContext()).edit().putString("mail", values.getString("mail")).apply();

                        Snackbar snack = Snackbar.make(vw_container,R.string.success_register, Snackbar.LENGTH_SHORT);
                        snack.setCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    Functions.checkLogin(getApplicationContext());
                                }
                            }
                        });
                        snack.show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("LS response", response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);
                    Snackbar.make(vw_container, responseString, Snackbar.LENGTH_SHORT).show();
                    Log.d("LS error", responseString);

                }


            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        et_email.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;

    }

}

