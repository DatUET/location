package com.example.save_location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;

public class LocationService extends BroadcastReceiver {
    public static final String ACTION_UPDATE = "UPDATE_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.endsWith(ACTION_UPDATE)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    Location location = result.getLastLocation();
                    try {
                        String time = System.currentTimeMillis() + "";
                        com.example.save_location.Location location1 = new com.example.save_location.Location(time, location.getLongitude(), location.getLatitude());
                        MapsActivity.getInstance().addLocation2DB(location1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
