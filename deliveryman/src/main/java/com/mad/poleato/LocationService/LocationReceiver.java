package com.mad.poleato.LocationService;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Double latitude;
    private Double longitude;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        PendingIntent locationIntent = PendingIntent.getBroadcast(context, 54321, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        fusedLocationProviderClient.removeLocationUpdates(locationIntent);
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
             latitude =  mLocationResult.getLastLocation().getLatitude();
             longitude = mLocationResult.getLastLocation().getLongitude();
             Intent i = new Intent("Coordinates");
             i.putExtra("latitude",latitude);
             i.putExtra("longitude",longitude);
             context.sendBroadcast(i);
            Log.d(TAG, "Location Received: " + this.mLocationResult.toString());
            Log.d(TAG, "Latitude: " + mLocationResult.getLastLocation().getLatitude() + "Longitude: " + mLocationResult.getLastLocation().getLongitude());
            UploadService.startActionUpload(context,latitude.toString(),longitude.toString());
        }
    }
}
