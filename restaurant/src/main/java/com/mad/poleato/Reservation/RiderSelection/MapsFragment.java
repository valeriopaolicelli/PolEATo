package com.mad.poleato.Reservation.RiderSelection;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;

import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;
import androidx.navigation.ui.NavigationUI;

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
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.Status;
import com.mad.poleato.Rider;
import com.onesignal.OneSignal;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;


/**
 * This fragment shows the available riders on the map
 */
public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;
    private GeoQuery geoQuery;

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

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

    private BottomNavigationView navigation;

    private String loggedID;

    private String localeShort;

    private ListView listView;
    private RiderListAdapter listAdapter;

    private SupportMapFragment mapFragment;

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    private HashMap<String, String> queueOrderRider;

    public MapsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to open the map view
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.activity_maps, container, false);

        navigation = getActivity().findViewById(R.id.navigation);

        /**
         * Value of Order FROM RESERVATION FRAGMENT
         */

        loggedID = MapsFragmentArgs.fromBundle(getArguments()).getLoggedId();
        reservation = MapsFragmentArgs.fromBundle(getArguments()).getReservation();

        Locale locale = Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);

        /**
         *
         */
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();


        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList = new HashMap<>();

        queueOrderRider= new HashMap<>();

        /** Obtain the SupportMapFragment and get notified when the map is ready to be used.*/
        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
        mapFragment.getView().setVisibility(View.GONE);


        referenceDB = FirebaseDatabase.getInstance().getReference("Map");

        geoFire = new GeoFire(referenceDB);

        /**
         * setup listview and adapter that will contain the list of riders;
         */
        riders = new HashMap<>();
        listView = (ListView) fragView.findViewById(R.id.rider_listview);
        listAdapter = new RiderListAdapter(getContext(), 0, reservation, loggedID);

        listView.setAdapter(listAdapter);

        displayLocation();

        return fragView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        /** Inflate the menu; this adds items to the action bar if it is present.*/
        inflater.inflate(R.menu.map_menu, menu);

        /** Button to show map */
        menu.findItem(R.id.map_id).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (mapFragment.getView().getVisibility() == View.VISIBLE) {
                    mapFragment.getView().setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_map_icon);
                } else {
                    mapFragment.getView().setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_list_icon);
                }

                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mapFragment.getView().getVisibility() == View.VISIBLE) {
                    mapFragment.getView().setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_map_icon);
                }else{
                    Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigateUp();
                }
                break;

        }
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Hide bottomBar for this fragment
        navigation.setVisibility(View.GONE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
    }

    /*
     * ********************************************************************************
     * body of activity
     * ********************************************************************************
     */

    /**
     * To shows the map with current restaurant and riders markers
     */
    private void displayLocation() {
        /*
         * retrieve the name of current restaurant to put the title to the marker in its map
         */
        final DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(currentUserID);
        DatabaseReference referenceRestaurantName= referenceRestaurant.child("Name");
        dbReferenceList.put("restaurantName", new MyDatabaseReference(referenceRestaurantName));

        dbReferenceList.get("restaurantName").setSingleValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                restaurant_name = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /**
         * retrieve coordinates of restaurant to put the marker in the map
         */

        DatabaseReference referenceRestaurantCoordinates= referenceRestaurant.child("Coordinates");
        dbReferenceList.put("restaurantCoordinates", new MyDatabaseReference(referenceRestaurantCoordinates));

        dbReferenceList.get("restaurantCoordinates").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
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

                                /**Move camera to this position*/
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitudeRest, longitudeRest), 15.0f));
                            }
                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
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

                                /**Move camera to this position*/
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
                if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
                    latitudeRest = Double.parseDouble(dataSnapshot.child("Latitude").getValue().toString());
                    longitudeRest = Double.parseDouble(dataSnapshot.child("Longitude").getValue().toString());
                    Log.d("Valerio", String.format("Restaurant location was changed: %f / %f", latitudeRest, longitudeRest));
                    //Update to firebase
                    geoFire.setLocation(currentUserID, new GeoLocation(latitudeRest, longitudeRest), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (reservation.getStatus().equals(Status.COOKING) && getContext() != null) {
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

                                /**Move camera to this position*/
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

        /**
         * retrieve rider coordinates and update the list and the map
         */
        geoQuery = geoFire.queryAtLocation(new GeoLocation(latitudeRest, longitudeRest), 2);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, final GeoLocation location) {
                final String riderID = key;

                if (!riderID.equals(currentUserID)) {
                    DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman/" + riderID);
                    dbReferenceList.put("rider", new MyDatabaseReference(referenceRider));

                    dbReferenceList.get("rider").setValueListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Busy") &&
                                    dataSnapshot.hasChild("IsActive") &&
                                    dataSnapshot.child("IsActive").getValue().toString().equals("true") &&
                                    reservation.getStatus().equals(Status.COOKING) &&
                                    getContext() != null) {

                                final double latRider = location.latitude;
                                final double longRider = location.longitude;

                                /*
                                 * retrieve the status of rider and how many orders has before your
                                 */
                                String status;
                                int numberOfOrder= 0;
                                if(dataSnapshot.child("Busy").getValue().toString().equals("true"))
                                    status = localeShort.equals("en") ? "Busy" : "Occupato";
                                else
                                    status = localeShort.equals("en") ? "Free" : "Libero";

                                // time of current order to deliver
                                String timeCurrentOrder= reservation.getTime();

                                /*
                                 * scan the orders of current rider and update the counter
                                 */
                                for(DataSnapshot dataSnapshotRequests : dataSnapshot.child("requests").getChildren()){
                                    if(dataSnapshotRequests.exists()) {

                                        // time of rider pending order
                                        String timeRequest = dataSnapshotRequests
                                                .child("deliveryTime").getValue().toString().split(" ")[1];

                                        if(timeRequest.compareTo(timeCurrentOrder) < 0)
                                            numberOfOrder++;
                                    }
                                }

                                /*
                                 * prepare the message containing the status (Busy or Free) and the number of pending orders before your
                                 * this message will be displayed in the list adapter and in the alert dialog,
                                 * when the rider marker is selected by the map
                                 */
                                String messageStatus= "";

                                if(status.equals("Busy") || status.equals("Free")) {
                                    if(numberOfOrder == 1)
                                        messageStatus = String.format("%s with %d pending order before yours", status, numberOfOrder);
                                    else if (numberOfOrder > 1)
                                        messageStatus = String.format("%s with %d pending orders before yours", status, numberOfOrder);
                                    else
                                        messageStatus = String.format("%s with no further pending order before your", status);
                                }
                                else if(status.equals("Occupato") || status.equals("Libero")){
                                    if(numberOfOrder == 1)
                                        messageStatus = String.format("%s con %d ordine prima del tuo", status, numberOfOrder);
                                    else if (numberOfOrder > 1)
                                        messageStatus = String.format("%s con %d ordini prima del tuo", status, numberOfOrder);
                                    else
                                        messageStatus = String.format("%s senza altri ordine prima del tuo", status);
                                }

                                 //this global map is necessary to give the message information also in the marker alert dialog
                                queueOrderRider.put(riderID, messageStatus);

                                if (!riders.containsKey(riderID)) {
                                    Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest, queueOrderRider.get(riderID));
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


                                /**Update restaurant map*/
                                geoFire.setLocation(riderID,
                                        new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
                                            @Override
                                            public void onComplete(String key, DatabaseError error) {
                                                if (reservation.getStatus().equals(Status.COOKING) &&
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
                if (!riderID.equals(currentUserID)) {
                    /**
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

                if (!riderID.equals(currentUserID)) {
                    DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman/" + riderID);
                    dbReferenceList.put("riderMoved", new MyDatabaseReference(referenceRider));

                    dbReferenceList.get("riderMoved").setValueListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Busy") &&
                                    dataSnapshot.hasChild("IsActive") &&
                                    dataSnapshot.child("IsActive").getValue().toString().equals("true") &&
                                    reservation.getStatus().equals(Status.COOKING) &&
                                    getContext() != null) {
                                final double latRider = location.latitude;
                                final double longRider = location.longitude;

                                final double distance = computeDistance(latitudeRest, longitudeRest, latRider, longRider);
                                if (distance <= 2) {
                                    /*
                                     * retrieve the status of rider and how many orders has before your
                                     */
                                    String status;
                                    int numberOfOrder= 0;
                                    if(dataSnapshot.child("Busy").getValue().toString().equals("true"))
                                        status = localeShort.equals("en") ? "Busy" : "Occupato";
                                    else
                                        status = localeShort.equals("en") ? "Free" : "Libero";

                                    // time of current order to deliver
                                    String timeCurrentOrder= reservation.getTime();

                                    /*
                                     * scan the orders of current rider and update the counter
                                     */
                                    for(DataSnapshot dataSnapshotRequests : dataSnapshot.child("requests").getChildren()){
                                        if(dataSnapshotRequests.exists()) {

                                            // time of rider pending order
                                            String timeRequest = dataSnapshotRequests
                                                    .child("deliveryTime").getValue().toString().split(" ")[1];

                                            if(timeRequest.compareTo(timeCurrentOrder) < 0)
                                                numberOfOrder++;
                                        }
                                    }

                                    /*
                                     * prepare the message containing the status (Busy or Free) and the number of pending orders before your
                                     * this message will be displayed in the list adapter and in the alert dialog,
                                     * when the rider marker is selected by the map
                                     */
                                    String messageStatus= "";

                                    if(status.equals("Busy") || status.equals("Free")) {
                                        if(numberOfOrder == 1)
                                            messageStatus = String.format("%s with %d pending order before yours", status, numberOfOrder);
                                        else if (numberOfOrder > 1)
                                            messageStatus = String.format("%s with %d pending orders before yours", status, numberOfOrder);
                                        else
                                            messageStatus = String.format("%s with no further pending order before your", status);
                                    }
                                    else if(status.equals("Occupato") || status.equals("Libero")){
                                        if(numberOfOrder == 1)
                                            messageStatus = String.format("%s con %d ordine prima del tuo", status, numberOfOrder);
                                        else if (numberOfOrder > 1)
                                            messageStatus = String.format("%s con %d ordini prima del tuo", status, numberOfOrder);
                                        else
                                            messageStatus = String.format("%s senza altri ordine prima del tuo", status);
                                    }

                                    //this global map is necessary to give the message information also in the marker alert dialog
                                    queueOrderRider.put(riderID, messageStatus);

                                    if (!riders.containsKey(riderID)) {
                                        Rider rider = new Rider(riderID, latRider, longRider, latitudeRest, longitudeRest, messageStatus);
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


                                    /**Update restaurant map*/
                                    geoFire.setLocation(riderID,
                                            new GeoLocation(latRider, longRider), new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {
                                                    if (reservation.getStatus().equals(Status.COOKING) &&
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
                DatabaseReference referenceMap = FirebaseDatabase.getInstance().getReference("Map");
                dbReferenceList.put("map", new MyDatabaseReference(referenceMap));

                dbReferenceList.get("map").setValueListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        for (DataSnapshot currentPosition : dataSnapshot.getChildren()) {
                            if (!currentPosition.getKey().equals(currentUserID)) {
                                final String riderID = currentPosition.getKey();
                                final double latRider = Double.parseDouble(currentPosition.child("l/0").getValue().toString());
                                final double longRider = Double.parseDouble(currentPosition.child("l/1").getValue().toString());

                                final double distance = computeDistance(latitudeRest, longitudeRest, latRider, longRider);
                                if (distance <= 2) {
                                    DatabaseReference referenceRider = FirebaseDatabase.getInstance()
                                            .getReference("deliveryman/" + riderID);
                                    dbReferenceList.put("riderGeoReady", new MyDatabaseReference(referenceRider));

                                    dbReferenceList.get("riderGeoReady")
                                            .setValueListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull final DataSnapshot dataSnapshotRider) {
                                            if (dataSnapshotRider.hasChild("Busy") &&
                                                    dataSnapshotRider.hasChild("IsActive") &&
                                                    dataSnapshotRider.child("IsActive").getValue().toString().equals("true") &&
                                                    reservation.getStatus().equals(Status.COOKING) &&
                                                    getContext() != null) {
                                                if (!riders.containsKey(riderID)) {
                                                    /*
                                                     * retrieve the status of rider and how many orders has before your
                                                     */
                                                    String status;
                                                    int numberOfOrder= 0;
                                                    if(dataSnapshotRider.child("Busy").getValue().toString().equals("true"))
                                                        status = localeShort.equals("en") ? "Busy" : "Occupato";
                                                    else
                                                        status = localeShort.equals("en") ? "Free" : "Libero";

                                                    // time of current order to deliver
                                                    String timeCurrentOrder= reservation.getTime();

                                                    /*
                                                     * scan the orders of current rider and update the counter
                                                     */
                                                    for(DataSnapshot dataSnapshotRequests : dataSnapshotRider.child("requests").getChildren()){
                                                        if(dataSnapshotRequests.exists()) {

                                                            // time of rider pending order
                                                            String timeRequest = dataSnapshotRequests
                                                                    .child("deliveryTime").getValue().toString().split(" ")[1];

                                                            if(timeRequest.compareTo(timeCurrentOrder) < 0)
                                                                numberOfOrder++;
                                                        }
                                                    }

                                                    /*
                                                     * prepare the message containing the status (Busy or Free) and the number of pending orders before your
                                                     * this message will be displayed in the list adapter and in the alert dialog,
                                                     * when the rider marker is selected by the map
                                                     */
                                                    String messageStatus= "";

                                                    if(status.equals("Busy") || status.equals("Free")) {
                                                        if(numberOfOrder == 1)
                                                            messageStatus = String.format("%s with %d pending order before yours", status, numberOfOrder);
                                                        else if (numberOfOrder > 1)
                                                            messageStatus = String.format("%s with %d pending orders before yours", status, numberOfOrder);
                                                        else
                                                            messageStatus = String.format("%s with no further pending order before your", status);
                                                    }
                                                    else if(status.equals("Occupato") || status.equals("Libero")){
                                                        if(numberOfOrder == 1)
                                                            messageStatus = String.format("%s con %d ordine prima del tuo", status, numberOfOrder);
                                                        else if (numberOfOrder > 1)
                                                            messageStatus = String.format("%s con %d ordini prima del tuo", status, numberOfOrder);
                                                        else
                                                            messageStatus = String.format("%s senza altri ordine prima del tuo", status);
                                                    }

                                                    //this global map is necessary to give the message information also in the marker alert dialog
                                                    queueOrderRider.put(riderID, messageStatus);

                                                    Rider rider = new Rider(riderID, latRider, longRider, latitudeRest,
                                                                                                    longitudeRest, messageStatus);
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
                                                                if (reservation.getStatus().equals(Status.COOKING) &&
                                                                        getContext() != null) {

                                                                    //Add marker
                                                                    if (riders.get(riderID).getMarker() != null)
                                                                        riders.get(riderID).getMarker().remove();
                                                                    Drawable icon = ContextCompat.getDrawable(getContext(),
                                                                                            R.drawable.ic_baseline_directions_bike_24px);
                                                                    BitmapDescriptor markerIcon = getMarkerIconFromDrawable(icon);
                                                                    riders.get(riderID).setMarker(mMap.addMarker(new MarkerOptions()
                                                                            .position(new LatLng(latRider, longRider))
                                                                            .title(riderID)
                                                                            .icon(markerIcon)
                                                                    ));
                                                                }
                                                            }
                                                        }); // end geofire set location
                                            }// end if check isActive, busy presence and status cooking
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }// end if check distance
                            } // end if check currentID
                        } // end for children
                    }// end onDataChange

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                Log.d("ValerioMap", "onGeoQueryReady -> AllReady");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("ValerioMap", "onGeoQueryError -> " + error.getMessage());
            }
        });
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
        if (!marker.getTitle().equals(restaurant_name)) {
            /* prepare the message to show
             * it contains the status of rider, the number of pending orders before yours and the question to confirm the choice
             */
            String message= "Rider " + queueOrderRider.get(marker.getTitle()).toLowerCase() + "\n" + this.getString(R.string.msg_rider_selected);
            final String riderID = marker.getTitle();
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(this.getString(R.string.rider_selected) + ": " + riderID);

            builder.setMessage(message);
            builder.setPositiveButton(this.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    reservation.setStat(getContext().getString(R.string.delivery));
                    reservation.setStatus(Status.DELIVERY);
                    FirebaseDatabase.getInstance().getReference("customers/"+reservation.getCustomerID()
                                                        +"/reservations/"+reservation.getOrder_id()+"/status/en").setValue("Delivering");
                    FirebaseDatabase.getInstance().getReference("customers/"+reservation.getCustomerID()
                                                        +"/reservations/"+reservation.getOrder_id()+"/status/it").setValue("In Consegna");
                    notifyRider(riderID);
                }
            });
            builder.setNegativeButton(this.getString(R.string.choice_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface arg0) {
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getContext().getColor(R.color.colorTextSubField));
                    dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getContext().getColor(R.color.colorTextSubField));
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getContext().getColor(R.color.colorPanelPrimary));
                }
            });
            dialog.show();

            return true;
        } else
            return false;
    }

    /**
     * to notify the rider for this order
     * @param riderID
     */
    private void notifyRider(final String riderID) {

        /** retrieve the restaurant information */
        final DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID);
        referenceRestaurant.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotRestaurant) {
                if (getContext() != null &&
                        dataSnapshotRestaurant.exists() &&
                        dataSnapshotRestaurant.hasChild("Address") &&
                        dataSnapshotRestaurant.hasChild("Name") &&
                        dataSnapshotRestaurant.hasChild("Phone") &&
                        dataSnapshotRestaurant.hasChild("reservations") &&
                        dataSnapshotRestaurant.child("reservations").hasChild(reservation.getOrder_id()) &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()).hasChild("status") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id() + "/status").hasChild("it") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id() + "/status").hasChild("en") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                + "/status/it").getValue().toString().equals("Preparazione") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                + "/status/en").getValue().toString().equals("Cooking")) {

                    DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID);
                    DatabaseReference reservationRider = referenceRider.child("requests").push();
                    final String addressRestaurant = dataSnapshotRestaurant.child("Address").getValue().toString();
                    final String nameRestaurant = dataSnapshotRestaurant.child("Name").getValue().toString();
                    final String phoneRestaurant = dataSnapshotRestaurant.child("Phone").getValue().toString();
                    reservationRider.child("addressCustomer").setValue(reservation.getAddress());
                    reservationRider.child("addressRestaurant").setValue(addressRestaurant);
                    reservationRider.child("CustomerID").setValue(reservation.getCustomerID());

                    //update the delivery status
                    reservationRider.child("nameRestaurant").setValue(nameRestaurant);
                    reservationRider.child("numberOfDishes").setValue(reservation.getNumberOfDishes());
                    reservationRider.child("orderID").setValue(reservation.getOrder_id());
                    reservationRider.child("restaurantID").setValue(loggedID);
                    reservationRider.child("nameCustomer").setValue(reservation.getName() + " " + reservation.getSurname());
                    reservationRider.child("totalPrice").setValue(reservation.getTotalPrice());
                    reservationRider.child("phoneCustomer").setValue(reservation.getPhone());
                    reservationRider.child("phoneRestaurant").setValue(phoneRestaurant);
                    reservationRider.child("delivering").setValue(false);

                    /** compose the date in the format YYYY/MM/DD HH:mm */
                    String[] date_components = reservation.getDate().split("/"); //format: dd/mm/yyyy
                    String timeStr = date_components[2] + "/" + date_components[1] + "/" + date_components[0] + " " +
                            reservation.getTime();
                    reservationRider.child("deliveryTime").setValue(timeStr);

                    FirebaseDatabase.getInstance().getReference("restaurants/"+
                            loggedID+"/reservations/"+reservation.getOrder_id()+"/status/en").setValue("Delivering");
                    FirebaseDatabase.getInstance().getReference("restaurants/"+
                            loggedID+"/reservations/"+reservation.getOrder_id()+"/status/it").setValue("In consegna");
                    sendNotification(riderID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "NotifyRandomRider -> retrieve restaurant info: " + databaseError.getMessage());
            }
        });
    }


    /**
     * It sends the notification to the selected rider
     * @param childID
     */
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

                    /** This is a Simple Logic to Send Notification different Device Programmatically.... */
                    send_email = childID;

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


    /**
     * To compute the distance between two points given their latitude and longitude. It exploits the Haversin formula
     * @param latitudeRest
     * @param longitudeRest
     * @param latitudeRider
     * @param longitudeRider
     * @return
     */
    public double computeDistance(double latitudeRest, double longitudeRest, double latitudeRider, double longitudeRider) {
        double startLat = latitudeRest;
        double endLat = latitudeRider;

        double startLong = longitudeRest;
        double endLong = longitudeRest;

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    /**
     * Haversin formula
     * @param val
     * @return
     */
    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();

        if (geoQuery != null)
            geoQuery.removeAllListeners();
    }


    @Override
    public void onStop() {
        super.onStop();

        navigation.setVisibility(View.VISIBLE);

        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();

        if (geoQuery != null)
            geoQuery.removeAllListeners();
    }
}
