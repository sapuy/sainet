package xyz.leosap.multiplace.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

import static com.loopj.android.http.AsyncHttpClient.log;


public class CreateActivity extends AppCompatActivity {

    private static final int REQUEST_READ_CONTACTS = 0;

    // UI references.

    private final int pick_photo = 666;
    private final int take_photo = 777;

    private View vw_progress;
    private View vw_container;
    private Bitmap bitmap;
    private ImageView iv_thumb;
    private double latitude, longitude;
    private boolean image_taken = false;
    Uri imageContent;
    private EditText et_place;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        vw_container = findViewById(R.id.vw_container);
        vw_progress = findViewById(R.id.vw_progress);
        et_place = (EditText) findViewById(R.id.et_place);
        iv_thumb = (ImageView) findViewById(R.id.iv_thumb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //GPS
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //return;
        }
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        log.d("LS LatLng last", "" + latitude + "," + longitude);


    }


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            log.d("LS LatLng", "" + latitude + "," + longitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

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
    public void onResume() {
        Functions.requestGPSEnabled(CreateActivity.this);
        super.onResume();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_image:
                AlertDialog.Builder builder = new AlertDialog.Builder(CreateActivity.this);
                builder.setMessage(R.string.msg_image)
                        .setTitle(R.string.title_image)
                        .setCancelable(true)
                        .setPositiveButton("Cámara", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Functions.goTakePhoto(CreateActivity.this, take_photo);
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Galeria", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Functions.goPickImage(CreateActivity.this, pick_photo);
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
            case R.id.bt_create:

                if (TextUtils.isEmpty(et_place.getText().toString().trim())) {
                    et_place.setError(getString(R.string.error_field_required));
                    et_place.requestFocus();
                    return;
                }
                try {

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap = Functions.resizeImageForImageView(bitmap);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos); //bm is the bitmap object
                    byte[] b = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);


                    JSONObject values = new JSONObject();
                    values.put("type", "place");
                    values.put("title", et_place.getText().toString().trim());
                    values.put("uid", Functions.getPreferences(getApplicationContext()).getString("uid", "0"));
                    values.put("lat", String.valueOf(latitude));
                    values.put("lon", String.valueOf(longitude));
                    values.put("field_image", "data:image/jpg;base64," + encodedImage);


                    create(values);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                break;
        }


    }

    private void create(JSONObject values) {
        //Log.d("LS http", values.toString());
        try {


            AsyncHttpClient client = new AsyncHttpClient();

            client.addHeader("Accept", "application/json");
            client.addHeader("Content-Type", "application/json");
            //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
            client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
            Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);
            client.setLoggingEnabled(true);
            client.setTimeout(600000);
            client.post(getApplicationContext(), Constants.URL_place, new StringEntity(values.toString()), "application/json", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);

                    Snackbar snack = Snackbar.make(vw_container, "Lugar creado correctamente", Snackbar.LENGTH_SHORT);
                    snack.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                finish();
                            }
                        }
                    });
                    snack.show();

                    Log.d("LS response", response.toString());
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);
                    Snackbar.make(vw_container, responseString, Snackbar.LENGTH_SHORT).show();

                    Log.d("LS error", responseString);

                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                    Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);
                    Snackbar.make(vw_container, throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    Log.d("LS error", throwable.getMessage());

                }


            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Picasso picasso = new Picasso.Builder(this).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        }).build();

        //Seleccionar foto desde galeria
        if (requestCode == pick_photo && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            imageContent = data.getData();


            picasso.load(imageContent).resize(Constants.image_width, Constants.image_height)
                    .centerCrop()
                    .into(iv_thumb);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageContent);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        //Tomar foto desde cámara
        if (requestCode == take_photo && resultCode == Activity.RESULT_OK) {
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "photo.jpg");


            Log.d("LS URI", String.valueOf(imageContent));
            imageContent = Uri.fromFile(file);
            picasso.load(imageContent).resize(Constants.image_width, Constants.image_height)
                    .centerCrop()
                    .into(iv_thumb);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

