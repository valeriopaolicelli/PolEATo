package com.mad.poleato;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;

import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;

import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


public class BackgroundLocationService extends Service {

    FusedLocationProviderClient fusedLocationProviderClient;
    IBinder mBinder = new LocalBinder();
    Notification notification;
    //  private PowerManager.WakeLock mWakeLock;
    private LocationRequest mLocationRequest;
    // Flag that indicates if a request is underway.
    private String CHANNEL_ID;
    Handler handler;

    public class LocalBinder extends Binder {
        public BackgroundLocationService getServiceInstance() {
            return BackgroundLocationService.this;
        }
    }

    public BackgroundLocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the update interval
        mLocationRequest.setInterval(Constants.UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);

        mLocationRequest.setSmallestDisplacement(Constants.DISPLACEMENT);


        handler = new Handler();
        CHANNEL_ID = "PolEATo";
        //create notification channel for foreground service
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(VISIBILITY_SECRET);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }


    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

       // PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

        Intent notificationIntent = new Intent(this, NavigatorActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText("Location")
                .setContentText("PolEATo is using your location in background")
                .setSmallIcon(R.drawable.location_icon)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
 /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.
        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
//        if (this.mWakeLock == null) { //**Added this
//            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "fabio:MyWakeLock");
//        }
//
//        if (!this.mWakeLock.isHeld()) { //**Added this
//            this.mWakeLock.acquire();
//        }


        // Request location updates using static settings
        final Intent intent_2 = new Intent(this, LocationReceiver.class);
        Log.d("Fabio", "Created pending intent");


        // Request location updates about deliveryman every 5 seconds
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                PendingIntent locationIntent = PendingIntent.getBroadcast(getApplicationContext(), 54321, intent_2, PendingIntent.FLAG_CANCEL_CURRENT);
                fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationIntent);
                handler.postDelayed(this,7000);
            }
        };

        handler.postDelayed(r,7000);

        return START_STICKY;

    }




    @Override
    public void onDestroy() {

        // Display the connection status
        // Toast.makeText(this, DateFormat.getDateTimeInstance().format(new Date()) + ":
        // Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();

//        if (this.mWakeLock != null) {
//            this.mWakeLock.release();
//            this.mWakeLock = null;
//        }

        //stopForeground(true);

        super.onDestroy();
    }
}

