package com.mad.poleato;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class UploadService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.mad.poleato.action.FOO";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.mad.poleato.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.mad.poleato.extra.PARAM2";

    FirebaseAuth firebaseAuth;

    public UploadService() {
        super("UploadService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
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
            handleActionUpload(latitude, longitude);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpload(String param1, String param2) {
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            String user = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference dbReference = FirebaseDatabase.getInstance()
                    .getReference("Map");

            Double latitude = Double.parseDouble(param1);
            Double longitude = Double.parseDouble(param2);
            GeoFire geoFire = new GeoFire(dbReference);
            geoFire.setLocation(user, new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                }
            });
            Log.d("fabio", "Upload completed with Lat & Long: " + latitude + " " + longitude);
        }
    }
}
