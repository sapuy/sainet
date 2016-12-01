package xyz.leosap.multiplace.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import xyz.leosap.multiplace.R;
import xyz.leosap.multiplace.adapters.adapterCardView;
import xyz.leosap.multiplace.adapters.adapterMapaInfoWindow;
import xyz.leosap.multiplace.common.Constants;
import xyz.leosap.multiplace.common.Functions;
import xyz.leosap.multiplace.objects.Place;

import static xyz.leosap.multiplace.R.id.container;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean doubleBack = false;
    private View vw_container, vw_progress;
    private FloatingActionButton fab;
    private GoogleMap mapa;
    private HashMap<String, Place> places = new HashMap<>();

    private RecyclerView recyclerView;
    private ArrayList<Place> places_array = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        Intent i = new Intent(getApplicationContext(), CreateActivity.class);
                        startActivity(i);
                    }
                });
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        ((TextView) (navigationView.getHeaderView(0).findViewById(R.id.nav_user))).setText(Functions.getPreferences(getApplicationContext()).getString("mail", ""));
        navigationView.setNavigationItemSelectedListener(this);

        vw_container = findViewById(R.id.content_main);
        vw_container = findViewById(R.id.vw_progress);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());




    }

    private void init_map() {


        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mapa = googleMap;

                if (mapa != null) {
                    // agregar_poligonos();
                    mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.latLngColombia, 10));
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //  servicio = new LatLng(Float.parseFloat(pref.getString(Constants.pref_address_lat, "0")), Float.parseFloat(pref.getString(Constants.pref_address_lng, "0")));
                            // mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(servicio, zoom));
                        }
                    }, 1000);
                    mapa.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                        @Override
                        public boolean onMarkerClick(Marker marker) {
                            if (marker.isInfoWindowShown()) {
                                marker.hideInfoWindow();
                            } else {
                                marker.showInfoWindow();
                            }
                            return true;
                        }
                    });
                    mapa.getUiSettings().setZoomControlsEnabled(false);

                    // mapa.getUiSettings().setMyLocationButtonEnabled(false);
                    //mapa.getUiSettings().setAllGesturesEnabled(false);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mapa.setMyLocationEnabled(false);


                }
            }
        });

    }


    public void carga_inicial() {

        init_map();

        HashMap<String, String> values = new HashMap<>();
        values.put("type", "place");
        values.put("author", Functions.getPreferences(getApplicationContext()).getString("uid", "0"));


        AsyncHttpClient client = new AsyncHttpClient();

        client.addHeader("Accept", "application/json");
        client.setTimeout(10000);
        client.addHeader("Content-Type", "application/json");
        //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
        client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
        // Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);

        client.get(Constants.URL_place, new RequestParams(values), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    //  Functions.showProgress(false, getApplicationContext(), vw_container, vw_progress);

                    JSONArray array = response.getJSONArray("list");
                    mapa.clear();
                    places.clear();
                    places_array.clear();
                    recyclerView.setAdapter(null);
                    for (int i = 0; i < array.length(); i++) {


                        JSONObject element = array.getJSONObject(i);

                        LatLng ltLg = new LatLng(element.getJSONObject("field_geofield").getDouble("lat"), element.getJSONObject("field_geofield").getDouble("lon"));
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(ltLg)
                                .draggable(false)
                                .visible(true);

                        Marker marker = mapa.addMarker(markerOptions);
                        final Place obj = new Place(element.getString("title"), element.getJSONObject("field_geofield").getDouble("lat"), element.getJSONObject("field_geofield").getDouble("lon"), "");

                        AsyncHttpClient client = new AsyncHttpClient();
                        client.setTimeout(10000);
                        client.addHeader("Accept", "application/json");
                        client.addHeader("Content-Type", "application/json");
                        //  client.addHeader("x-CSRF-Token", Functions.getPreferences(getApplicationContext()).getString("token", Constants.default_token));
                        client.setBasicAuth(Constants.WS_user, Constants.WS_pass);
                        // Functions.showProgress(true, getApplicationContext(), vw_container, vw_progress);

                        client.get(element.getJSONObject("field_image").getJSONObject("file").getString("uri"), null, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                try {
                                    obj.setImage(response.getString("url"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                                Log.d("LS response image", response.toString());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                                Log.d("LS error", responseString);

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {

                                Log.d("LS error", throwable.getMessage());

                            }
                        });


                        places.put(marker.getId(), obj);
                        places_array.add(obj);


                    }

                    adapterMapaInfoWindow adapter = new adapterMapaInfoWindow(getApplicationContext(), places);
                    mapa.setInfoWindowAdapter(null);
                    mapa.setInfoWindowAdapter(adapter);


                    adapterCardView adapter_card = new adapterCardView(MainActivity.this, places_array);
                    recyclerView.setAdapter(adapter_card);



                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("LS response", response.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject responseString) {
                //  Functions.showProgress(false, getApplicationContext(), fab, vw_progress);
                if (throwable != null) {
                    Snackbar.make(fab, "Error al conectarse con el servidor", Snackbar.LENGTH_SHORT).show();
                    //  Log.d("LS error",  throwable.getMessage());
                }


            }


        });


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBack) {
                System.gc();
                finish();
                return;
            }
            this.doubleBack = true;
            Snackbar snack = Snackbar.make(fab, R.string.double_back_exit, Snackbar.LENGTH_SHORT);
            snack.setCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar snackbar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        doubleBack = false;
                    }

                }
            });
            snack.show();


        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        switch (item.getItemId()) {
            case R.id.nav_create:
                fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        Intent i = new Intent(getApplicationContext(), CreateActivity.class);
                        startActivity(i);
                    }
                });


                break;
            case R.id.nav_logout:
                Snackbar.make(fab, R.string.prompt_logout, Snackbar.LENGTH_LONG)
                        .setAction("Cerrar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                fab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                                    @Override
                                    public void onHidden(FloatingActionButton fab) {
                                        Functions.logout(getApplicationContext());
                                    }
                                });

                            }
                        })
                        .show();

                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick(View v){
        switch (v.getId()){
            case R.id.sw_map:
                SwitchCompat sw=(SwitchCompat)v;

                if(sw.isChecked()){
                    findViewById(R.id.map).setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);

                }else{
                    findViewById(R.id.map).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                break;
        }
    }

    @Override
    public void onResume() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show();
            }
        }, 1000);
        carga_inicial();
        super.onResume();
    }
}
