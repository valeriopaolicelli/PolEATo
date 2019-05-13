package com.mad.poleato;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;

public class LocationReceiver extends BroadcastReceiver {

    private String TAG = this.getClass().getSimpleName();

    private LocationResult mLocationResult;
    FusedLocationProviderClient fusedLocationProviderClient;

    DatabaseReference ref;
    Double latitude;
    Double longitude;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Need to check and grab the Intent's extras like so
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        PendingIntent locationIntent = PendingIntent.getBroadcast(context, 54321, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if(LocationResult.hasResult(intent)) {
            this.mLocationResult = LocationResult.extractResult(intent);
             latitude =  mLocationResult.getLastLocation().getLatitude();
             longitude = mLocationResult.getLastLocation().getLongitude();
            Log.d(TAG, "Location Received: " + this.mLocationResult.toString());
            Log.d(TAG, "Latitude: " + mLocationResult.getLastLocation().getLatitude() + "Longitude: " + mLocationResult.getLastLocation().getLongitude());
            UploadService.startActionUpload(context,latitude.toString(),longitude.toString());
            fusedLocationProviderClient.removeLocationUpdates(locationIntent);
        }
    }
}
