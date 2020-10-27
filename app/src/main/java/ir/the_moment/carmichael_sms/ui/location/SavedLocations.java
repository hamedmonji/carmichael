package ir.the_moment.carmichael_sms.ui.location;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.mR;

public class SavedLocations extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "map";
    private static final String ALL_COORDINATES = "all_coordinates";
    private GoogleMap map;
    private String[] currentLocationsData;
    private FloatingActionButton lastLocation;
    private FloatingActionButton setLocationUpdates;
    private Marker lastMarker;
    private float color = BitmapDescriptorFactory.HUE_BLUE;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_locations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        lastLocation = findViewById(R.id.last_location);
        setLocationUpdates = findViewById(R.id.set_location_updates);
        SharedPreferences prefs = getSharedPreferences(mR.CurrentDevice.number,MODE_PRIVATE);
        currentLocationsData = prefs.getString(ALL_COORDINATES,"").split(mR.DATA_SEPARATOR);
        setListeners();
        initMap();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                    Toast.makeText(SavedLocations.this, R.string.location_not_ready, Toast.LENGTH_SHORT).show();
                }
            }
        });

        setLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_saved_locations,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_all)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences prefs = getSharedPreferences(mR.CurrentDevice.number,MODE_PRIVATE);
                        prefs.edit().putString(ALL_COORDINATES,null).commit();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel,null)
                .show();
        return true;
    }
}
