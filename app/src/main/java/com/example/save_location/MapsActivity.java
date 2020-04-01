package com.example.save_location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "MainActivity";
    int LOCATION_REQUEST_CODE = 10001;
    boolean stopService = false;

    private GoogleMap mMap;
    Button cleanLocationButton, stopServiceButton;
    FusedLocationProviderClient fusedLocationProviderClient;
    static MapsActivity instance;
    DatabaseHandler databaseHandler;
    List<Polyline> polylineList;
    PolylineOptions polylineOptions;
    Intent intent;
    private LocationCallback mLocationCallback;
    List<Location> locationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        instance = this;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addControl();
    }

    private void drawPolyline() {
        if (!polylineList.isEmpty()) {
            for (Polyline i : polylineList) {
                i.remove();
            }
        }
        locationList.clear();
        locationList = databaseHandler.getAllLocations();
        Log.d("size", locationList.size() + "");
        if (locationList.size() > 1) {
            for (int i = 1; i < locationList.size(); i++) {
                LatLng latLng1 = new LatLng(locationList.get(i - 1).getLatitude(), locationList.get(i - 1).getLongitude());
                LatLng latLng2 = new LatLng(locationList.get(i).getLatitude(), locationList.get(i).getLongitude());
//            mMap.addMarker(new MarkerOptions().position(latLng1));
//            mMap.addMarker(new MarkerOptions().position(latLng2));
                polylineOptions = new PolylineOptions().width(20).color(Color.CYAN).add(latLng1, latLng2);
                polylineList.add(mMap.addPolyline(polylineOptions));
            }
            //mMap.addPolyline(new PolylineOptions().width(20).color(Color.CYAN).add(latLng1, latLng2));
        }
    }

    private void addControl() {
        databaseHandler = new DatabaseHandler(this);
        cleanLocationButton = findViewById(R.id.cleanLocationButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);
        polylineList = new ArrayList<>();
        locationList = new ArrayList<>();
        intent = new Intent(MapsActivity.this, LocationService.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Log.d(TAG, "onLocationResult: location error");
                    return;
                }

                List<android.location.Location> locations = locationResult.getLocations();

                LocationResultHelper helper = new LocationResultHelper(MapsActivity.this, locations);

                helper.showNotification();

                helper.saveLocationResults();

                addLocation2DB(new com.example.save_location.Location(System.currentTimeMillis() + "", locations.get(0).getLongitude(), locations.get(0).getLatitude()));
                drawPolyline();

//                Log.d(TAG, "onLocationResult: " + location.getLatitude() + " \n" +
//                        location.getLongitude());


            }
        };
    }

    private void addEvent() {
        requestBatchLocationUpdates();
        cleanLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseHandler.deleteAllLocation();
                for (Polyline i : polylineList) {
                    i.remove();
                }
            }
        });

        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
                stopService = true;
            }
        });
    }

    private void startLocationService() {
        //start background location service

        Intent intent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, intent);
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

    }

    private void stopLocationService() {
        //stop background location service

        Intent intent = new Intent(this, LocationService.class);
        stopService(intent);
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show();

    }

    void addLocation2DB(final Location location) {
        MapsActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                databaseHandler.addLocation(location);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        addEvent();
        LatLng latLng = new LatLng(21.0138424,105.7976838);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));
    }

    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestBatchLocationUpdates();
        } else {
            askLocationPermission();
        }
    }


    private void requestBatchLocationUpdates() {

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(4000);

        locationRequest.setMaxWaitTime(15 * 1000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
    }

    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "askLocationPermission: you should show an alert dialog...");
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                requestBatchLocationUpdates();
            } else {
                //Permission not granted
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!stopService) {
            startLocationService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(LocationResultHelper.KEY_LOCATION_RESULTS)) {
            //mOutputText.setText(LocationResultHelper.getSavedLocationResults(this));
        }
    }
}