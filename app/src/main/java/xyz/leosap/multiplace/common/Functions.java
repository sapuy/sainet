package xyz.leosap.multiplace.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.activities.MainActivity;
import xyz.leosap.multiplace.activities.RegisterActivity;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created by LeoSap on 27/11/2016.
 */

public class Functions {
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences("ls_pref", Context.MODE_PRIVATE);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isPasswordValid(String password) {

        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static void showProgress(final boolean show, Context context, final View container, final View progress) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            container.setVisibility(show ? View.GONE : View.VISIBLE);
            container.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    container.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            container.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public static boolean mayRequestContacts(final Context context, View v) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (context.checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (((Activity) context).shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(v, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            ((Activity) context).requestPermissions(new String[]{READ_CONTACTS}, 1);
                        }
                    });
        } else {
            ((Activity) context).requestPermissions(new String[]{READ_CONTACTS}, 1);
        }
        return false;
    }

    public static void checkLogin(Context context) {

        Log.d("LS logued", String.valueOf(getPreferences(context).getBoolean("logued", false)));
        if (getPreferences(context).getBoolean("logued", false)) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

    }

    public static void logout(Context context) {
        getPreferences(context).edit().clear().apply();
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    public static void goPickImage(Context context, int code) {
        System.gc();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ((Activity) context).startActivityForResult(intent, code);

    }

    public static void goTakePhoto(Context context, int code) {
        System.gc();

        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri  = Uri.parse("file:///sdcard/photo.jpg");
        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
        ((Activity) context).startActivityForResult(cameraIntent, code);

    }
    public static void requestGPSEnabled(final Context context) {

        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
            builder.setMessage(R.string.error_gps_disabled_msg)
                    .setTitle(R.string.error_gps_disabled)
                    .setCancelable(false)
                    .setPositiveButton("Activar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                            context.startActivity(new Intent(action));
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            ((Activity) context).finish();

                        }
                    })
            .show();

        }
    }
    public static Bitmap resizeImageForImageView(Bitmap bitmap) {
        int scaleSize =100;
        Bitmap resizedBitmap = null;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int newWidth = -1;
        int newHeight = -1;
        float multFactor = -1.0F;
        if(originalHeight > originalWidth) {
            newHeight = scaleSize ;
            multFactor = (float) originalWidth/(float) originalHeight;
            newWidth = (int) (newHeight*multFactor);
        } else if(originalWidth > originalHeight) {
            newWidth = scaleSize ;
            multFactor = (float) originalHeight/ (float)originalWidth;
            newHeight = (int) (newWidth*multFactor);
        } else if(originalHeight == originalWidth) {
            newHeight = scaleSize ;
            newWidth = scaleSize ;
        }
        resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        return resizedBitmap;
    }

}

