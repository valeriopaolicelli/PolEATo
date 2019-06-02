package com.mad.poleato.LocationService;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * This service upload the coordinates on Firebase Database
 */
public class UploadService extends IntentService {
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    private static final String ACTION_FOO = "com.mad.poleato.action.FOO";

    private Double previous_latitude;
    private Double previous_longitude;
    private static final String EXTRA_PARAM1 = "com.mad.poleato.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.mad.poleato.extra.PARAM2";

    private FirebaseAuth firebaseAuth;


    private LatLng oldLocation;


    public UploadService() {
        super("UploadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpload(Context context, String param1, String param2) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        try {
            context.startService(intent);
        }catch (Exception e){
            Log.d("ServiceException", e.getMessage());
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String latitude = intent.getStringExtra(EXTRA_PARAM1);
            final String longitude = intent.getStringExtra(EXTRA_PARAM2);



            LatLng currLocation = new LatLng(Double.parseDouble(latitude),
                                                Double.parseDouble(longitude));

            if (oldLocation != null) {
                //check if the rider moved of at least 'X' m.
                double d = LocationUtilities.computeDistance(oldLocation, currLocation) * 1000;
                if (d <= 7)
                    return;
            }

            handleActionUpload(latitude, longitude);
            oldLocation = currLocation;
        }
    }

    /**
     * Handle action upload in the provided background thread with the provided
     * parameters. This method will upload the coordinates on Firebase
     */
    private void handleActionUpload(String param1, String param2) {
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            String user = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference dbReference = FirebaseDatabase.getInstance()
                    .getReference("Map");

            final Double latitude = Double.parseDouble(param1);
            final Double longitude = Double.parseDouble(param2);
            GeoFire geoFire = new GeoFire(dbReference);
            geoFire.setLocation(user, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.d("fabio", "Upload completed with Lat & Long: " + latitude + " " + longitude);

                }
            });
            Log.d("fabio", "Upload completed with Lat & Long: " + latitude + " " + longitude);
        }
    }

}
