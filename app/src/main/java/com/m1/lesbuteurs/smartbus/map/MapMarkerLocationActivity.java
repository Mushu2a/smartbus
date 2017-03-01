package com.m1.lesbuteurs.smartbus.map;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.m1.lesbuteurs.smartbus.AppController;
import com.m1.lesbuteurs.smartbus.R;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.m1.lesbuteurs.smartbus.config.AppConfig.URL_LOCALISE;

public class MapMarkerLocationActivity extends AppCompatActivity {

    String mUpdateTimeLocation;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private GoogleApiClient mGoogleApiClient;
    private Marker mapMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_google_map);

        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("La clé API google map est manquante");
        }

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Erreur !", Toast.LENGTH_SHORT).show();
        }
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;

        LatLng latLng = new LatLng(44.0572554, 4.7598298);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 5);
        map.animateCamera(cameraUpdate);

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewMarkers();
                        map.clear();
                    }
                });
            }
        }, 0, 15000);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public void viewMarkers() {

        JsonArrayRequest localiseBus = new JsonArrayRequest(URL_LOCALISE, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
            try {

                for (int i = 0; i < response.length(); i++) {

                    JSONObject locations = (JSONObject) response.get(i);

                    String username = locations.getString("username");
                    Double latitude = Double.parseDouble(locations.getString("latitude"));
                    Double longitude = Double.parseDouble(locations.getString("longitude"));
                    String date = locations.getString("updated_at");

                    MarkerOptions options = new MarkerOptions();
                    int colors[] = {IconGenerator.STYLE_ORANGE, IconGenerator.STYLE_DEFAULT,
                            IconGenerator.STYLE_PURPLE, IconGenerator.STYLE_GREEN,
                            IconGenerator.STYLE_BLUE, IconGenerator.STYLE_RED,
                            IconGenerator.STYLE_ORANGE, IconGenerator.STYLE_GREEN,
                            IconGenerator.STYLE_PURPLE, IconGenerator.STYLE_BLUE};

                    int randomColor = colors[new Random().nextInt(colors.length)];

                    IconGenerator iconFactory = new IconGenerator(MapMarkerLocationActivity.this);
                    iconFactory.setStyle(randomColor);
                    options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(username)));
                    options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

                    LatLng currentLatLng = new LatLng(latitude, longitude);
                    options.position(currentLatLng);
                    Marker mapMarker = map.addMarker(options);
                    mapMarker.setTitle(date);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(localiseBus);
    }
}

