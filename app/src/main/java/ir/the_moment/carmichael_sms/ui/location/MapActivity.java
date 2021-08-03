package ir.the_moment.carmichael_sms.ui.location;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.Calendar;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.location.GetLocation;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "getLocation";
    private static final String ALL_COORDINATES = "all_coordinates";
    private GoogleMap map;
    private String[] currentLocationsData;
    private Message message;
    private FloatingActionButton lastLocation;
    private FloatingActionButton setLocationUpdates;
    private Marker lastMarker;
    private float color = BitmapDescriptorFactory.HUE_BLUE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        message = getIntent().getParcelableExtra(mR.KEY_MESSAGES);

        if (message != null) {
            lastLocation = findViewById(R.id.last_location);
            setLocationUpdates = findViewById(R.id.set_location_updates);
            setListeners();
            String rawData = message.getExtra(GetLocation.DATA_KEY_LOCATION_DATA);
            setUpMap(rawData);
        }
    }

    private void setUpMap(String rawData) {
        if (rawData != null ) {
            Log.i(TAG,rawData);
            currentLocationsData = rawData.split(mR.DATA_SEPARATOR);
            Log.i(TAG, Arrays.toString(currentLocationsData));
        }

        if (map == null) {
            initMap();
        }else {
            initLocations();
        }
    }

    private void initLocations() {
        boolean isLast = false;
        for (int i = 0; i < currentLocationsData.length; i++) {
            String locationData = currentLocationsData[i];
            if (i == currentLocationsData.length - 1)
                isLast = true;
            showLocations(locationData, isLast);
        }
        changeMapLocation(currentLocationsData[currentLocationsData.length -1]);
    }

    private void updateLocation() {
        String locationData = currentLocationsData[currentLocationsData.length-1];
        LatLng latLng = getLatLng(locationData);
        if (lastMarker == null){
            addMarker(latLng,null,color);
        }else {
            animateMarker(lastMarker,latLng,false);
        }
        changeMapLocation(locationData);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        message = getIntent().getParcelableExtra(mR.KEY_MESSAGES);
        String rawData = message.getExtra(GetLocation.DATA_KEY_LOCATION_DATA);
        setUpMap(rawData);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void showLocations(String locationData,boolean isLast) {
        LatLng latLng = getLatLng(locationData);
        color = BitmapDescriptorFactory.HUE_RED;
        String title = getMarkerTitle(locationData);
        if (isLast){
            color = BitmapDescriptorFactory.HUE_BLUE;
        }
        addMarker(latLng,title,color);
    }

    private void setListeners() {
        lastLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( map != null && currentLocationsData != null){
                    changeMapLocation(currentLocationsData[currentLocationsData.length -1]);
                }else {
                    Toast.makeText(MapActivity.this, R.string.location_not_ready, Toast.LENGTH_SHORT).show();
                }
            }
        });

        setLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mR.CurrentDevice.number == null) {
                    mR.CurrentDevice.number = message.sender;
                }
                if (mR.CurrentDevice.number == null) return;
                SharedPreferences prefs = getSharedPreferences(mR.CurrentDevice.number,MODE_PRIVATE);
                String allCoordiantes = prefs.getString(ALL_COORDINATES,null);

                if (allCoordiantes != null) {
                    Log.i(TAG, "onClick: ");
                    String[] allData = allCoordiantes.split(mR.DATA_SEPARATOR);
                    for (int i = 0; i < allData.length; i++) {
                        String locationData = allData[i];
                        LatLng latLng = getLatLng(locationData);
                        float color = BitmapDescriptorFactory.HUE_RED;
                        String title = getMarkerTitle(locationData);
                        if (i == allData.length -1){
                            color = BitmapDescriptorFactory.HUE_BLUE;
                        }
                        addMarker(latLng,title,color);
                    }
                    changeMapLocation(currentLocationsData[currentLocationsData.length -1]);
                }
                Log.i(TAG, "onClick: ");

            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady: ");
        map = googleMap;
        map.clear();
        initLocations();
    }

    private void changeMapLocation(String coordinates){
        Log.i(TAG, "changeMapLocation: ");
        if (map != null) {
            CameraPosition target = CameraPosition.builder().target(getLatLng(coordinates)).zoom(18).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(target));
        }
    }

    private String getMarkerTitle(String locationData){
        if (locationData != null) {
            String[] data = locationData.split(",");
            if (data.length >= 2) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(data[2]));
                String time = calendar.getTime().toString();
                return time.substring(0,time.indexOf("GMT")).trim();
            }
        }
        return null;
    }

    private LatLng getLatLng(String coordinates){
        String[] location = coordinates.split(",");
        LatLng latLng = null;
        if (location .length >= 2 ) {
            latLng = new LatLng(Double.parseDouble(location[0]), Double.parseDouble(location[1]));
        }
        return latLng;
    }

    private void addMarker(LatLng location,String title,float color){
        if (location != null) {
            MarkerOptions options = new MarkerOptions();
            options.position(location).title(title);
            options.icon(BitmapDescriptorFactory.defaultMarker(color));
            if (map != null) {
                lastMarker = map.addMarker(options);
            }
        }
    }

    private void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
