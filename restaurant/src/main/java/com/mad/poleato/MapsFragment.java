package com.mad.poleato;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.LocationListener;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.navigation.Navigation;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.Status;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    //Play Service Location
    private static final int MY_PERMISSION_REQUEST_CODE = 0001;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 000001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000; // 5 seconds
    private static int FASTEST_INTERVAL = 3000; // 3 seconds
    private static int DISPLACEMENT = 10;

    DatabaseReference referenceDB;
    GeoFire geoFire;
    private String restaurant_name;
    private double latitudeRest;
    private double longitudeRest;
    private Marker restaurantMarker;
    private HashMap<String, Rider> riders;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private View fragView;
    private Reservation reservation;

    private String loggedID;

    private String localeShort;

    private ListView listView;
    private RiderListAdapter listAdapter;

    private FloatingActionButton mapButton;
    private SupportMapFragment mapFragment;


    public MapsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.activity_maps, container, false);

        /**
         * Value of Order FROM RESERVATION FRAGMENT
         */

        loggedID = MapsFragmentArgs.fromBundle(getArguments()).getLoggedId();
        reservation= MapsFragmentArgs.fromBundle(getArguments()).getReservation();

        Locale locale= Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);

        /**
         *
         */
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.GONE);

        mapButton= fragView.findViewById(R.id.map_button);

        referenceDB= FirebaseDatabase.getInstance().getReference("Map");
        //ref = FirebaseDatabase.getInstance().getReference("restaurants").child(currentUserID);
        //referenceRiders = FirebaseDatabase.getInstance().getReference("deliveryman");
        geoFire = new GeoFire(referenceDB);

        /*
         * setup listview and adapter that will contain the list of riders;
         */
        riders = new HashMap<>();
        listView = (ListView) fragView.findViewById(R.id.rider_listview);
        listAdapter = new RiderListAdapter(getContext(), 0, reservation, loggedID);

        listView.setAdapter(listAdapter);

        setUpLocation();


        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();

        handleButton();
    }

    private void handleButton() {
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mapFragment.getView().getVisibility() == View.VISIBLE) {
                    mapFragment.getView().setVisibility(View.GONE);
                    mapButton.setImageResource(R.mipmap.map_icon_round);
                }
                else {
                    mapFragment.getView().setVisibility(View.VISIBLE);
                    mapButton.setImageResource(R.mipmap.list_icon_round);
                }
            }
        });
    }

    /*
     * Functions related to location and google play services
     */

    private void setUpLocation() {
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // Request routine permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
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
        mGoogleApiClient= new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if(resultCode != ConnectionResult.SUCCESS){
            if(GoogleApiAvailability.getInstance().isUserResolvableError(resultCode))
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            else{
                Toast.makeText(getContext(), "This device is not suppoted", Toast.LENGTH_SHORT).show();

                /**
                 * GO FROM MAPSFRAGMENT to RESERVATION
                 */
                Navigation.findNavController(fragView).navigate(R.id.action_mapsFragment_id_to_reservation_id);
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
        mMap.setOnMarkerClickListener(this);
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
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        /*
         * retrieve the name of current restaurant to put the title to the marker in its map
         */
        DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(currentUserID);
        referenceRestaurant.child("Name").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                restaurant_name = dataSnapshot.getValue().toString();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                restaurant_name = dataSnapshot.getValue().toString();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //the restaurant must have the name
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                restaurant_name = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
         * retrieve coordinates of restaurant to put the marker in the map
         */

        referenceRestaurant.child("Coordinates").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                                //Add marker
                                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.restaurant_icon);
                                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                if (restaurantMarker != null)
                                    restaurantMarker = null;

                                restaurantMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitudeRest, longitudeRest))
                                        .title(restaurant_name)
                                        .icon(markerIcon)
                                );

                                //Move camera to this position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeRest, longitudeRest), 15.0f));
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                                //Add marker
                                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.restaurant_icon);
                                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                if (restaurantMarker != null)
                                    restaurantMarker = null;

                                restaurantMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitudeRest, longitudeRest))
                                        .title(restaurant_name)
                                        .icon(markerIcon)
                                );

                                //Move camera to this position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeRest, longitudeRest), 15.0f));
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // the restaurant must have an address and coordinates
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if(reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                                //Add marker
                                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.restaurant_icon);
                                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                if (restaurantMarker != null)
                                    restaurantMarker = null;

                                restaurantMarker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(latitudeRest, longitudeRest))
                                        .title(restaurant_name)
                                        .icon(markerIcon)
                                );

                                //Move camera to this position
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeRest, longitudeRest), 15.0f));
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*
         * retrieve rider coordinates and update the list and the map
         */


        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitudeRest, longitudeRest), 6);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
              @Override
              public void onKeyEntered(String key, final GeoLocation location) {
                  final String riderID = key;

                  if(!riderID.equals(currentUserID)) {
                      DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman/" + riderID);
                      referenceRider.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                              if (dataSnapshot.hasChild("Busy") &&
                                      dataSnapshot.child("Busy").getValue().toString().equals("false") &&
                                        dataSnapshot.hasChild("IsActive") &&
                                        dataSnapshot.child("IsActive").getValue().toString().equals("true") &&
                                        reservation.getStatus().equals(Status.COOKING) &&
                                        getContext() != null) {
                                  final double latRider = location.latitude;
                                  final double longRider = location.longitude;
                                  if (!riders.containsKey(riderID)) {
                                      Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest);
                                      riders.put(riderID, rider);
                                      listAdapter.addRider(riders.get(riderID));
                                  } else {
                                      riders.get(riderID).setLatitude(latRider);
                                      riders.get(riderID).setLongitude(longRider);
                                      riders.get(riderID).setDistance(latitudeRest, longitudeRest);
                                      for (int i = 0; i < listAdapter.getCount(); i++) {
                                          if (listAdapter.getItem(i).getId().equals(riderID)) {
                                              listAdapter.getItem(i).setLatitude(latRider);
                                              listAdapter.getItem(i).setLongitude(longRider);
                                              listAdapter.getItem(i).setDistance(latitudeRest, longitudeRest);
                                          }
                                      }
                                  }

                                  listAdapter.notifyDataSetChanged();


                                  //Update restaurant map
                                  geoFire.setLocation(riderID,
                                          new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
                                              @Override
                                              public void onComplete(String key, DatabaseError error) {
                                                  if (dataSnapshot.child("Busy").getValue().toString().equals("false") &&
                                                          reservation.getStatus().equals(Status.COOKING) &&
                                                          getContext() != null) {

                                                      //Add marker
                                                      if (riders.get(riderID).getMarker() != null)
                                                          riders.get(riderID).getMarker().remove();
                                                      Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_directions_bike_24px);
                                                      BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                                      riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
                                                              .position(new LatLng(latRider, longRider))
                                                              .title(riderID)
                                                              .icon(markerIcon)
                                                      ));
                                                  }
                                              }
                                          });
                              } else if(dataSnapshot.hasChild("Busy") &&
                                      dataSnapshot.child("Busy").getValue().toString().equals("true") &&
                                      reservation.getStatus().equals(Status.COOKING) &&
                                      getContext() != null) {
                                  /*
                                   * remove busy rider if is in the adapter
                                   */
                                  if (riders.containsKey(riderID)) {
                                      riders.get(riderID).getMarker().remove();
                                      riders.remove(riderID);
                                      listAdapter.removeRider(riderID);
                                      listAdapter.notifyDataSetChanged();
                                  }
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {
                              Log.d("ValerioMap", "onCancelled referenceRider -> " + databaseError.getMessage());
                          }
                      });
                  }
                  Log.d("ValerioMap", String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
              }

              @Override
              public void onKeyExited(String key) {
                  String riderID = key;
                  if(!riderID.equals(currentUserID)) {
                      /*
                       * remove busy rider if is in the adapter
                       */
                      if (riders.containsKey(riderID)) {
                          riders.get(riderID).getMarker().remove();
                          riders.remove(riderID);
                          listAdapter.removeRider(riderID);
                          listAdapter.notifyDataSetChanged();
                      }
                  }
                  Log.d("ValerioMap", String.format("Key %s is no longer in the search area", key));
              }

              @Override
              public void onKeyMoved(String key, final GeoLocation location) {
                  final String riderID = key;

                  if(!riderID.equals(currentUserID)) {
                      DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman/" + riderID);
                      referenceRider.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                              if (dataSnapshot.hasChild("Busy") &&
                                      dataSnapshot.child("Busy").getValue().toString().equals("false") &&
                                        dataSnapshot.hasChild("IsActive") &&
                                        dataSnapshot.child("IsActive").getValue().toString().equals("true") &&
                                        reservation.getStatus().equals(Status.COOKING) &&
                                        getContext() != null) {
                                  final double latRider = location.latitude;
                                  final double longRider = location.longitude;
                                  if (!riders.containsKey(riderID)) {
                                      Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest);
                                      riders.put(riderID, rider);
                                      listAdapter.addRider(riders.get(riderID));
                                  } else {
                                      riders.get(riderID).setLatitude(latRider);
                                      riders.get(riderID).setLongitude(longRider);
                                      riders.get(riderID).setDistance(latitudeRest, longitudeRest);
                                      for (int i = 0; i < listAdapter.getCount(); i++) {
                                          if (listAdapter.getItem(i).getId().equals(riderID)) {
                                              listAdapter.getItem(i).setLatitude(latRider);
                                              listAdapter.getItem(i).setLongitude(longRider);
                                              listAdapter.getItem(i).setDistance(latitudeRest, longitudeRest);
                                          }
                                      }
                                  }

                                  listAdapter.notifyDataSetChanged();


                                  //Update restaurant map
                                  geoFire.setLocation(riderID,
                                          new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
                                              @Override
                                              public void onComplete(String key, DatabaseError error) {
                                                  if (dataSnapshot.child("Busy").getValue().toString().equals("false") &&
                                                          reservation.getStatus().equals(Status.COOKING) &&
                                                          getContext() != null) {

                                                      //Add marker
                                                      if (riders.get(riderID).getMarker() != null)
                                                          riders.get(riderID).getMarker().remove();
                                                      Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_directions_bike_24px);
                                                      BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                                      riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
                                                              .position(new LatLng(latRider, longRider))
                                                              .title(riderID)
                                                              .icon(markerIcon)
                                                      ));
                                                  }
                                              }
                                          });
                              } else if(dataSnapshot.hasChild("Busy") &&
                                      dataSnapshot.child("Busy").getValue().toString().equals("true") &&
                                      reservation.getStatus().equals(Status.COOKING) &&
                                      getContext() != null){
                                  /*
                                   * remove busy rider if is in the adapter
                                   */
                                  if (riders.containsKey(riderID)) {
                                      riders.get(riderID).getMarker().remove();
                                      riders.remove(riderID);
                                      listAdapter.removeRider(riderID);
                                      listAdapter.notifyDataSetChanged();
                                  }
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {
                              Log.d("ValerioMap", "onCancelled referenceRider -> " + databaseError.getMessage());
                          }
                      });
                  }
                  Log.d("ValerioMap", String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
              }

              @Override
              public void onGeoQueryReady() {
//                  DatabaseReference referenceMap= FirebaseDatabase.getInstance().getReference("Map");
//                  referenceMap.addValueEventListener(new ValueEventListener() {
//                     @Override
//                     public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
//                         for (DataSnapshot currentPosition : dataSnapshot.getChildren()) {
//                             if (!currentPosition.getKey().equals(currentUserID)) {
//                                 final String riderID= currentPosition.getKey();
//                                 final double latRider = Double.parseDouble(currentPosition.child("l/0").getValue().toString());
//                                 final double longRider = Double.parseDouble(currentPosition.child("l/1").getValue().toString());
//
//                                 DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman/" + riderID);
//                                 referenceRider.addValueEventListener(new ValueEventListener() {
//                                     @Override
//                                     public void onDataChange(@NonNull final DataSnapshot dataSnapshotRider) {
//                                         if (dataSnapshotRider.hasChild("Busy") &&
//                                                 dataSnapshotRider.child("Busy").getValue().toString().equals("false") &&
//                                                 dataSnapshotRider.hasChild("IsActive") &&
//                                                 dataSnapshotRider.child("IsActive").getValue().toString().equals("true") &&
//                                                 reservation.getStatus().equals(Status.COOKING) &&
//                                                 getContext() != null) {
//                                             if (!riders.containsKey(riderID)) {
//                                                 Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest);
//                                                 riders.put(riderID, rider);
//                                                 listAdapter.addRider(riders.get(riderID));
//                                             } else {
//                                                 riders.get(riderID).setLatitude(latRider);
//                                                 riders.get(riderID).setLongitude(longRider);
//                                                 riders.get(riderID).setDistance(latitudeRest, longitudeRest);
//                                                 for (int i = 0; i < listAdapter.getCount(); i++) {
//                                                     if (listAdapter.getItem(i).getId().equals(riderID)) {
//                                                         listAdapter.getItem(i).setLatitude(latRider);
//                                                         listAdapter.getItem(i).setLongitude(longRider);
//                                                         listAdapter.getItem(i).setDistance(latitudeRest, longitudeRest);
//                                                     }
//                                                 }
//                                             }
//
//                                             listAdapter.notifyDataSetChanged();
//
//
//                                             //Update restaurant map
//                                             geoFire.setLocation(riderID,
//                                                     new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
//                                                         @Override
//                                                         public void onComplete(String key, DatabaseError error) {
//                                                             if (dataSnapshotRider.child("Busy").getValue().toString().equals("false") &&
//                                                                     reservation.getStatus().equals(Status.COOKING) &&
//                                                                     getContext() != null) {
//
//                                                                 //Add marker
//                                                                 if (riders.get(riderID).getMarker() != null)
//                                                                     riders.get(riderID).getMarker().remove();
//                                                                 Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_directions_bike_24px);
//                                                                 BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
//                                                                 riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
//                                                                         .position(new LatLng(latRider, longRider))
//                                                                         .title(riderID)
//                                                                         .icon(markerIcon)
//                                                                 ));
//                                                             }
//                                                         }
//                                                     }); // end geofire set location
//                                         } // end if busy= false
//                                         else if(dataSnapshotRider.hasChild("Busy") &&
//                                                 dataSnapshotRider.child("Busy").getValue().toString().equals("true") &&
//                                                 reservation.getStatus().equals(Status.COOKING) &&
//                                                 getContext() != null){
//                                             /*
//                                              * remove busy rider if is in the adapter
//                                              */
//                                             if (riders.containsKey(riderID)) {
//                                                 riders.get(riderID).getMarker().remove();
//                                                 riders.remove(riderID);
//                                                 listAdapter.removeRider(riderID);
//                                                 listAdapter.notifyDataSetChanged();
//                                             }
//                                         }// end else busy= true
//                                     }
//                                     @Override
//                                     public void onCancelled (@NonNull DatabaseError databaseError){
//
//                                     }
//                                 });
//                             } // end if check currentID
//                         } // end for children
//                     }// end onDataChange
//
//                      @Override
//                      public void onCancelled (@NonNull DatabaseError databaseError){
//
//                      }
//                  });
                  Log.d("ValerioMap", "onGeoQueryReady -> AllReady");
              }

              @Override
              public void onGeoQueryError(DatabaseError error) {
                  Log.d("ValerioMap", "onGeoQueryError -> " + error.getMessage());
              }
        });


//        final Map<String, DatabaseReference> refRider= new HashMap<>();
//        final Map<String, GeoFire> geoFireRider= new HashMap<>();


//
//        DatabaseReference referenceRider= FirebaseDatabase.getInstance().getReference("deliveryman");
//        referenceRider.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(final DataSnapshot ds : dataSnapshot.getChildren()){
//                    if(ds.hasChild("Busy") &&
//                            ds.hasChild("IsActive") &&
//                            ds.child("IsActive").getValue().toString().equals("true") &&
//                            (reservation.getStat().equals("Cooking") || reservation.getStat().equals("Preparazione"))  &&
//                            getContext() != null) {
//
//                        final String riderID = ds.getKey();
//
//                        if (ds.child("Busy").getValue().toString().equals("false")) {
//
//                            if( ds.hasChild("Busy") &&
//                                    ds.child("Busy").getValue().toString().equals("false") &&
//                                    reservation.getStatus().equals(Status.COOKING) &&
//                                    getContext() != null) {
//
//                                if (!refRider.containsKey(riderID))
//                                refRider.put(riderID, FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID));
//
//                                if (!geoFireRider.containsKey(riderID))
//                                geoFireRider.put(riderID, new GeoFire(refRider.get(riderID)));
//
//                                geoFireRider.get(riderID).getLocation("Map", new LocationCallback() {
//                                    @Override
//                                    public void onLocationResult(String key, GeoLocation location) {
//                                        if (location != null) {
//                                            /*
//                                             * Add or update rider location to adapter and restaurant map
//                                             */
//                                            final double latRider = location.latitude;
//                                            final double longRider = location.longitude;
//
//                                            if (!riders.containsKey(riderID)) {
//                                                Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest);
//                                                riders.put(riderID, rider);
//                                                listAdapter.addRider(riders.get(riderID));
//                                            } else {
//                                                riders.get(riderID).setLatitude(latRider);
//                                                riders.get(riderID).setLongitude(longRider);
//                                                riders.get(riderID).setDistance(latitudeRest, longitudeRest);
//                                                for (int i = 0; i < listAdapter.getCount(); i++) {
//                                                    if (listAdapter.getItem(i).getId().equals(riderID)) {
//                                                        listAdapter.getItem(i).setLatitude(latRider);
//                                                        listAdapter.getItem(i).setLongitude(longRider);
//                                                        listAdapter.getItem(i).setDistance(latitudeRest, longitudeRest);
//                                                    }
//                                                }
//                                            }
//
//                                            listAdapter.notifyDataSetChanged();
//
//
//                                            //Update restaurant map
//                                            geoFire.setLocation("Map/" + riderID,
//                                                    new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
//                                                        @Override
//                                                        public void onComplete(String key, DatabaseError error) {
//                                                            if (ds.hasChild("Busy") &&
//                                                                    ds.child("Busy").getValue().toString().equals("false") &&
//                                                                    reservation.getStatus().equals(Status.COOKING) &&
//                                                                    getContext() != null) {
//
//                                                                //Add marker
//                                                                if (riders.get(riderID).getMarker() != null)
//                                                                    riders.get(riderID).getMarker().remove();
//                                                                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_directions_bike_24px);
//                                                                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
//                                                                riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
//                                                                        .position(new LatLng(latRider, longRider))
//                                                                        .title(riderID)
//                                                                        .icon(markerIcon)
//                                                                ));
//                                                            }
//                                                        }
//                                                    });
//
//                                        } else {
//                                            Log.d("ValerioMap", riderID + String.format(" -> There is no location for key %s in GeoFire", key));
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onCancelled(DatabaseError databaseError) {
//                                        Log.d("ValerioMap", "There was an error getting the GeoFire location: " + databaseError);
//                                    }
//                                });
//                            }
//                        }
//                        else if (ds.child("Busy").getValue().toString().equals("true")){ // rider is busy
//                            /*
//                             * remove busy rider if is in the adapter
//                             */
//                            if(riders.containsKey(riderID)){
//                                riders.get(riderID).getMarker().remove();
//                                riders.remove(riderID);
//                                ref.child("Map").child(riderID).setValue("");
//                                listAdapter.removeRider(riderID);
//                                listAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!marker.getTitle().equals(restaurant_name)){
            final String riderID= marker.getTitle();
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(this.getString(R.string.rider_selected) + ": " + riderID);

            builder.setMessage(this.getString(R.string.msg_rider_selected));
            builder.setPositiveButton(this.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID);
                    referenceRider.runTransaction(new Transaction.Handler() {
                          @NonNull
                          @Override
                          public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                              mutableData.child("Busy").setValue(true);
                              FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(reservation.getOrder_id()).child("status").child("en").setValue("Delivering");
                              FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(reservation.getOrder_id()).child("status").child("it").setValue("In consegna");
                              reservation.setStat(getContext().getString(R.string.delivery));
                              reservation.setStatus(Status.DELIVERY);
                              notifyRider(riderID);
                              return Transaction.success(mutableData);
                          }

                          @Override
                          public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                              // Transaction completed
                              Log.d("Valerio", "postTransaction:onComplete:" + databaseError);
                          }
                      }
                    );

//                    FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID).child("Busy").setValue(true);
//                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(reservation.getOrder_id()).child("status").child("en").setValue("Delivering");
//                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(reservation.getOrder_id()).child("status").child("it").setValue("In consegna");
//                    reservation.setStat(getContext().getString(R.string.delivery));
//                    reservation.setStatus(Status.DELIVERY);
//                    notifyRider(riderID);
//                    /**
//                     * GO FROM MAPSFRAGMENT to RESERVATION
//                     */
//                    Navigation.findNavController(fragView).navigate(R.id.action_mapsFragment_id_to_reservation_id);
                }
            });
            builder.setNegativeButton(this.getString(R.string.choice_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return true;
        }
        else
            return false;
    }

    private void notifyRider(final String riderID) {

        /* retrieve the restaurant information */
        final DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID);
        referenceRestaurant.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotRestaurant) {
                if(dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                +"/status/it").getValue().toString().equals("In consegna") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                +"/status/en").getValue().toString().equals("Delivering")) {
                    DatabaseReference referenceRider= FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID);
                    DatabaseReference reservationRider = referenceRider.child("reservations").push();

                    if (dataSnapshotRestaurant.exists() &&
                            dataSnapshotRestaurant.hasChild("Address") &&
                            dataSnapshotRestaurant.hasChild("Name") &&
                            dataSnapshotRestaurant.hasChild("Phone")) {

                        final String addressRestaurant = dataSnapshotRestaurant.child("Address").getValue().toString();
                        final String nameRestaurant = dataSnapshotRestaurant.child("Name").getValue().toString();
                        final String phoneRestaurant = dataSnapshotRestaurant.child("Phone").getValue().toString();
                        reservationRider.child("addressCustomer").setValue(reservation.getAddress());
                        reservationRider.child("addressRestaurant").setValue(addressRestaurant);
                        reservationRider.child("CustomerID").setValue(reservation.getCustomerID());
                        //update the delivery status
                        reservationRider.child("delivering").setValue(false);
                        reservationRider.child("nameRestaurant").setValue(nameRestaurant);
                        reservationRider.child("numberOfDishes").setValue(reservation.getNumberOfDishes());
                        reservationRider.child("orderID").setValue(reservation.getOrder_id());
                        reservationRider.child("restaurantID").setValue(loggedID);
                        reservationRider.child("nameCustomer").setValue(reservation.getName() + " " + reservation.getSurname());
                        reservationRider.child("totalPrice").setValue(reservation.getTotalPrice());
                        reservationRider.child("phoneCustomer").setValue(reservation.getPhone());
                        reservationRider.child("phoneRestaurant").setValue(phoneRestaurant);
                        reservationRider.child("time").setValue(reservation.getTime());
                        sendNotification(riderID);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "NotifyRandomRider -> retrieve restaurant info: " + databaseError.getMessage());
            }
        });
    }

    private void sendNotification(final String childID) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_email;

                    //This is a Simple Logic to Send Notification different Device Programmatically....
                    send_email= childID;

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic YjdkNzQzZWQtYTlkYy00MmIzLTg0NDUtZmQ3MDg0ODc4YmQ1");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"a2d0eb0d-4b93-4b96-853e-dcfe6c34778e\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"Delivery\": \"New order\"},"
                                + "\"contents\": {\"en\": \"New order to deliver\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

        /**
         * GO FROM MAPSFRAGMENT to RESERVATION
         */
        Navigation.findNavController(fragView).navigate(R.id.action_mapsFragment_id_to_reservation_id);
    }
}
