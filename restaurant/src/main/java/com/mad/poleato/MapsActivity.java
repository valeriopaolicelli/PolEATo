package com.mad.poleato;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationListener;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity
                          implements OnMapReadyCallback,
                                     GoogleApiClient.ConnectionCallbacks,
                                     GoogleApiClient.OnConnectionFailedListener,
                                     LocationListener{

    private GoogleMap mMap;

    //Play Service Location
    private static final int MY_PERMISSION_REQUEST_CODE= 0001;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST= 000001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL= 5000; // 5 seconds
    private static int FASTEST_INTERVAL= 3000; // 3 seconds
    private static int DISPLACEMENT= 10;

    DatabaseReference ref;
    GeoFire geoFire;
    private String restaurant_name;
    private double latitudeRest;
    private double longitudeRest;
    private Marker restaurantMarker;
    private HashMap<String, Rider> riders;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private ListView listView;
    private RiderListAdapter listAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);

        ref= FirebaseDatabase.getInstance().getReference("restaurants").child(currentUserID);
        geoFire= new GeoFire(ref);

        /*
         * setup listview and adapter that will contain the list of riders;
         */
        riders= new HashMap<>();
        listView = (ListView) findViewById(R.id.rider_listview);
        listAdapter= new RiderListAdapter(this, 0);

        listView.setAdapter(listAdapter);

        setUpLocation();
    }

    /*
     * Functions related to location and google play services
     */

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Request routine permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }
        else{
            if(checkPlayServices()){
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void createLocationRequest() {
        mLocationRequest= new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else{
                Toast.makeText(this, "This device is not suppoted", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(checkPlayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation= location;
        displayLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
     * ********************************************************************************
     * body of activity
     * ********************************************************************************
     */

    private void displayLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    /*    mLastLocation= LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation != null){
            latitude= mLastLocation.getLatitude();
            longitude= mLastLocation.getLongitude();*/

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                restaurant_name= dataSnapshot.child("Name").getValue().toString();
                latitudeRest= Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                longitudeRest= Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                Log.d( "Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                //Update to firebase
                geoFire.setLocation("Map/"+currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        //Add marker
                        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.restaurant_icon);
                        BitmapDescriptor markerIcon= getMarkerIconFromDrawable(icon);
                        if(restaurantMarker != null)
                            restaurantMarker= null;

                        restaurantMarker= mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitudeRest, longitudeRest))
                                .title(restaurant_name)
                                .icon(markerIcon));

                        //Move camera to this position
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeRest, longitudeRest), 15.0f));

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "Firebase onCancelled in MapsActivity");
            }
        });

        DatabaseReference referenceRider= FirebaseDatabase.getInstance().getReference("deliveryman");
        referenceRider.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(ds.hasChild("Latitude") &&
                            ds.hasChild("Longitude") &&
                            ds.hasChild("Busy")
                            && ds.child("Busy").getValue().toString().equals("false")) {

                        final String riderID = ds.getKey();
                        final double latRider = Double.parseDouble(ds.child("Latitude").getValue().toString());
                        final double longRider = Double.parseDouble(ds.child("Longitude").getValue().toString());

                        if(!riders.containsKey(riderID)) {
                            Rider rider= new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest);
                            riders.put(riderID, rider);
                            listAdapter.addRider(riders.get(riderID));
                        }
                        else{
                            riders.get(riderID).setLatitude(latRider);
                            riders.get(riderID).setLongitude(longRider);
                            riders.get(riderID).setDistance(latitudeRest, longitudeRest);
                            for(int i=0; i < listAdapter.getCount(); i++) {
                                if (listAdapter.getItem(i).getId().equals(riderID)) {
                                    listAdapter.getItem(i).setLatitude(latRider);
                                    listAdapter.getItem(i).setLongitude(longRider);
                                    listAdapter.getItem(i).setDistance(latitudeRest, longitudeRest);
                                }
                            }
                        }

                        listAdapter.notifyDataSetChanged();

                        Log.d("Valerio", String.format("Rider %s location was changed: %f / %f", riderID, latRider, longRider));
                        //Update to firebase
                        geoFire.setLocation("Map/" + riderID,
                                new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //Add marker
                                        if(riders.get(riderID).getMarker() != null)
                                            riders.get(riderID).setMarker(null);

                                        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_directions_bike_24px);
                                        BitmapDescriptor markerIcon= getMarkerIconFromDrawable(icon);
                                        riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latRider, longRider))
                                                .title(riderID)
                                                .icon(markerIcon)));
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    /*    }
        else
            Log.d("Valerio", "Cannot get your location");*/
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
