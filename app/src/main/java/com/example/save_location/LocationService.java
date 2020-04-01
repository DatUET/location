package com.example.save_location;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location";
    FusedLocationProviderClient fusedLocationProviderClient;
    DatabaseHandler databaseHandler;
    Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        databaseHandler = new DatabaseHandler(this);
    }

    void addLocation2DB(final com.example.save_location.Location location) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                databaseHandler.addLocation(location);
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getLastLocation();
        return START_NOT_STICKY;

    }

    private void getLastLocation() {
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Example Service")
                .setContentIntent(pendingIntent);
        notification = builder.build();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(android.location.Location location) {
                if (location != null) {
                    //We have a location
                    addLocation2DB(new com.example.save_location.Location(System.currentTimeMillis() + "", location.getLongitude(), location.getLatitude()));
                    notification = builder.setContentText(location.getLongitude() + "/" + location.getLatitude()).build();
                } else  {
                    Log.d("abc", "onSuccess: Location was null...");
                }
            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("abc", "onFailure: " + e.getLocalizedMessage() );
            }
        });
        startForeground(1, notification);
    }
}
