package com.med.locationvoiture.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationHelper {
    
    private Context context;
    private LocationCallback callback;
    private FusedLocationProviderClient fusedLocationClient;
    
    public interface LocationCallbackInterface {
        void onLocationReceived(double latitude, double longitude, String address);
    }
    
    public LocationHelper(Context context, LocationCallbackInterface callback) {
        this.context = context;
        this.callback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    String address = lat + ", " + lng;
                    callback.onLocationReceived(lat, lng, address);
                }
            }
        };
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdates(1)
            .build();
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    public void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(callback);
    }
}