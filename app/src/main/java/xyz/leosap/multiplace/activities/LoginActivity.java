package xyz.leosap.multiplace.activities;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.common.Constants;
import xyz.leosap.multiplace.common.Functions;


public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.
    private AutoCompleteTextView et_email;
    private EditText et_password;
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

                                HashMap<String, String> values = new HashMap<>();
                                values.put("mail", email);
                                loginFb(values);


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
        setContentView(R.layout.activity_login);
        // Set up the login form.
        et_email = (AutoCompleteTextView) findViewById(R.id.et_email);

        et_password = (EditText) findViewById(R.id.et_password);


        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            case R.id.bt_login:
                // Reset errors.
                et_email.setError(null);

                et_password.setError(null);

                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    et_email.setError(getString(R.string.error_field_required));
                    et_email.requestFocus();
                    return;
                }


                if (TextUtils.isEmpty(password)) {
                    et_password.setError(getString(R.string.error_field_required));
                    et_password.requestFocus();
                    return;
                }


                if (!Functions.isEmailValid(email)) {
                    et_email.setError(getString(R.string.error_invalid_email));
                    et_email.requestFocus();
                    return;
                }


                if (!Functions.isPasswordValid(password)) {
                    et_password.setError(getString(R.string.error_invalid_password));
                    et_password.requestFocus();
                    return;
                }


                //Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);
                try {
                    JSONObject values = new JSONObject();
                    values.put("username", email);
                    values.put("pass", password);
                    login(values);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                break;
        }


    }

    private void login(JSONObject values) {
        try {


            AsyncHttpClient client = new AsyncHttpClient();

            client.addHeader("Accept", "application/json");
            client.addHeader("Content-Type", "application/json");
            //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
            client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
            Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);


            client.post(getApplicationContext(), Constants.URL_user_login, new StringEntity(values.toString()), "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try {
                        Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);

                        Functions.getPreferences(getApplicationContext()).edit().putBoolean("logued", true).apply();
                        Functions.getPreferences(getApplicationContext()).edit().putString("uid", response.getJSONObject("user").getString("uid")).apply();
                        Functions.getPreferences(getApplicationContext()).edit().putString("mail", response.getJSONObject("user").getString("mail")).apply();

                        Snackbar snack = Snackbar.make(vw_container, response.getString("message"), Snackbar.LENGTH_SHORT);
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

    private void loginFb(HashMap<String, String> values) {


        AsyncHttpClient client = new AsyncHttpClient();

        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
        client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
        Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);

        client.get(Constants.URL_user, new RequestParams(values), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);
                    Functions.getPreferences(getApplicationContext()).edit().putBoolean("logued", true).apply();
                    Functions.getPreferences(getApplicationContext()).edit().putString("uid", response.getJSONArray("list").getJSONObject(0).getString("uid")).apply();
                    Functions.getPreferences(getApplicationContext()).edit().putString("mail", response.getJSONArray("list").getJSONObject(0).getString("mail")).apply();

                    Snackbar snack = Snackbar.make(vw_container, getString(R.string.success_login), Snackbar.LENGTH_SHORT);
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
                new ArrayAdapter<>(LoginActivity.this,
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

