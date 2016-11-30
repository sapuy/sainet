package xyz.leosap.multiplace.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.common.Constants;
import xyz.leosap.multiplace.common.Functions;
import xyz.leosap.multiplace.objects.Place;

/**
 * Created by leonardo-pc on 1/09/2016.
 */
public class adapterMapaInfoWindow implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private LayoutInflater inflater = null;
    private HashMap<String, Place> places;

    public adapterMapaInfoWindow(Context context, HashMap<String, Place> places) {
        this.context = context;
        this.places = places;
        this.inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {


        Place place = places.get(marker.getId());

        View v = inflater.inflate(R.layout.list_info_window, null);


        TextView tv1 = (TextView) v.findViewById(R.id.tv_nombre);
        TextView tv2 = (TextView) v.findViewById(R.id.tv_ubicacion);
        final ImageView iv1 = (ImageView) v.findViewById(R.id.imageView4);
        tv1.setText(place.getName());
        tv2.setText(place.getLat() + "," + place.getLng());



        Log.d("LS img",place.getImage());
        Picasso.with(context)
                .load(place.getImage())
                //.config(Bitmap.Config.RGB_565)
                .error(R.drawable.icon)
                .fit()
                .placeholder(R.drawable.icon)
                .centerInside()
                .into(iv1);



        return v;
    }
}
