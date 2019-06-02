package com.mad.poleato.Ride;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.LocationService.LocationUtilities;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;


/**
 * A simple {@link Fragment} subclass.
 */
public class RideFragment extends Fragment implements OnMapReadyCallback {

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    //two constants for frequency update tuning
    private static final double MIN_DISTANCE_LOC_UPDATE = 8.0;
    private static final double ARRIVED_DISTANCE = 8.0; //if closer to 8 meters than notify arrived

    private Toast myToast;
    private Activity hostActivity;
    private View fragView;

    //layout elements
    private Map<String, TextView> tv_Fields;
    private Button button_map;
    private ImageButton show_more_button;

    //to set the visibility to gone when there are no ride available
    private ImageView emptyView;
    private CardView rideCardView;
    private FrameLayout rideFrameLayout;


    //this flag is to avoid multiple order that will override the maps
    private boolean isRunning;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    //location data
    private GoogleMap mMap;
    private LatLng customerPosition;
    private LatLng restaurantPosition;
    private Geocoder geocoder;

    //current distance and duration of the ride
    private String currDistance;
    private String currDuration;

    // object representing the actual ride (if presents)
    private Ride ride;
    private MyDatabaseReference deliveryReservationReference;

    /*the latest location received from the backgound service. It is needed to check if the rider is moving
    if not the directions are not updated each time */
    private LatLng oldLocation;

    // this flag is to notify that the next retrieved distance must be uploaded on firebase (the total distance of the ride before it starts)
    private Boolean isKmUploadNeeded;

    private ProgressDialog progressDialog; //ran only the first time to wait for the distance to be uploaded on FireBase

    private SupportMapFragment mapFragment;





    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_SHORT);

        //initially the rider is free from work
        isRunning = false;

        //to retrieve latitude and longitude from address
        geocoder = new Geocoder(hostActivity);

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();


        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        createMap();

        tv_Fields = new HashMap<>();

        //this value is always false. Can be changed only in 2 points of the code:
        //      when for the fist time this reservation is received (Firebase `totKm` field not present) or during status switching
        isKmUploadNeeded = false;
    }


//    private void logout(){
//        //logout
//        Log.d("matte", "Logout");
//        FirebaseAuth.getInstance().signOut();
//        OneSignal.setSubscription(false);
//
//        //go to signIn activity
//        //Navigation.findNavController(fragView).navigate(R.id.action_rides_id_to_signInActivity); //TODO mich
//        getActivity().finish();
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.ride_layout, container, false);

        //collects all the TextView inside the HashMap tv_Fields and attach the listeners
        collectFields();

        //download the ride data from firebase
        retrieveOrderInfo();

        show_empty_view();

        return fragView;
    }


    private void collectFields() {

        tv_Fields.put("address", (TextView) fragView.findViewById(R.id.deliveryAddress_tv));
        tv_Fields.put("name", (TextView) fragView.findViewById(R.id.customerName_tv));
        tv_Fields.put("restaurant", (TextView) fragView.findViewById(R.id.restaurant_tv));
        tv_Fields.put("phone", (TextView) fragView.findViewById(R.id.phone_tv));
        tv_Fields.put("dishes", (TextView) fragView.findViewById(R.id.dishes_tv));
        tv_Fields.put("hour", (TextView) fragView.findViewById(R.id.time_tv));
        tv_Fields.put("price", (TextView) fragView.findViewById(R.id.cost_tv));
        tv_Fields.put("distance", (TextView) fragView.findViewById(R.id.distance_tv));
        tv_Fields.put("duration", (TextView) fragView.findViewById(R.id.duration_tv));

        rideCardView = (CardView) fragView.findViewById(R.id.rideCardView);
        rideFrameLayout = (FrameLayout) fragView.findViewById(R.id.rideFrameLayout);
        emptyView = (ImageView) fragView.findViewById(R.id.ride_empty_view);

        button_map = (Button) fragView.findViewById(R.id.button_map);
        button_map.setOnClickListener(new OnClickButtonMap());

        show_more_button = (ImageButton) fragView.findViewById(R.id.showMoreButton);
        show_more_button.setOnClickListener(new OnClickShowMore());

        //initialize visibility
        show_empty_view();

    }


    // shown when there are no active orders
    private void show_empty_view() {

        rideCardView.setVisibility(View.GONE);
        rideFrameLayout.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }


    // shown when there is an active order
    private void show_order_view() {

        emptyView.setVisibility(View.GONE);
        rideCardView.setVisibility(View.VISIBLE);
        rideFrameLayout.setVisibility(View.VISIBLE);
    }


    private class OnClickShowMore implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            /*FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("show_more_fragment");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            ShowMoreFragment showMoreFrag = new ShowMoreFragment();
            Bundle bundle = new Bundle();
            //pass the restaurant info
            bundle.putString("name", ride.getNameRestaurant());
            bundle.putString("address", ride.getAddressRestaurant());
            bundle.putString("phone", ride.getPhoneRestaurant());
            showMoreFrag.setArguments(bundle);
            showMoreFrag.show(ft, "show_more_fragment");*/

            Bundle bundle = new Bundle();
            //pass the restaurant info
            bundle.putString("name", ride.getNameRestaurant());
            bundle.putString("address", ride.getAddressRestaurant());
            bundle.putString("phone", ride.getPhoneRestaurant());

            ShowMoreFragment showmore = new ShowMoreFragment();
            showmore.setArguments(bundle);
            showmore.show(getFragmentManager(), "show_more");
        }
    }


    private void createMap() {

        //get the map fragment
       /* FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("map_fragment");
        if (prev != null) {
            //mapFragment.onDestroy();
            ((SupportMapFragment) prev).getMapAsync(this);
            //ft.remove(prev);
            return;
        }*/
        mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        //mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment, "map_fragment").commit();
        }

        //when the map is ready the callback method OnMapReady will be called
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        //check if permission for location if permission for location was grant
        if (ActivityCompat.checkSelfPermission(hostActivity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(hostActivity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("matte", "permission 0 granted");

                } else {
                    Log.d("matte", "permission 0 not granted");
                    //permission denied
                    myToast.setText(hostActivity.getString(R.string.permission_denied));
                    myToast.show();
                    //TODO ?
                }
                return;
            }
        }

    }


    private void retrieveOrderInfo() {

        deliveryReservationReference = new MyDatabaseReference(FirebaseDatabase.getInstance()
                                            .getReference("deliveryman/" + currentUserID + "/ride"));
        deliveryReservationReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() &&
                        getContext() != null &&
                        !isRunning &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("CustomerID") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("phoneCustomer") &&
                        dataSnapshot.hasChild("phoneRestaurant") &&
                        dataSnapshot.hasChild("deliveryTime") &&
                        dataSnapshot.hasChild("startTime") &&
                        dataSnapshot.hasChild("requestKey")) {


                    readOrder(dataSnapshot); // read order from FireBase and fill the layout (it also hide the empy view)
                    updateMapButton(); // update the button based on the ride status
                    readTargetAddresses(); //retrieve location for customer and restaurant
                    // Acquire a reference to the system Location Manager
                    hostActivity.registerReceiver(locationReceiver,new IntentFilter("Coordinates"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d("matte", "[ERROR] Database error");
            }
        });

    }


    private void terminateRide(String endTime) {

        //save the completed ride into the history
        DatabaseReference historyReference = FirebaseDatabase.getInstance().getReference("deliveryman/" + currentUserID + "/history").push();
        historyReference.child("orderID").setValue(ride.getOrderID());
        historyReference.child("addressRestaurant").setValue(ride.getAddressRestaurant());
        historyReference.child("nameRestaurant").setValue(ride.getNameRestaurant());
        historyReference.child("totalPrice").setValue(ride.getTotalPrice());
        historyReference.child("numberOfDishes").setValue(ride.getNumberOfDishes());
        historyReference.child("expectedTime").setValue(ride.getDeliveryTime());
        historyReference.child("startTime").setValue(ride.getStartTime());
        historyReference.child("endTime").setValue(endTime);
        historyReference.child("totKm").setValue(ride.getKm());
        if(ride.getStatus() == RideStatus.BACKWARD)
            historyReference.child("outcome").setValue("FAILURE");
        else
            historyReference.child("outcome").setValue("SUCCESS");

        //remove the ride
        DatabaseReference reservationReference = FirebaseDatabase.getInstance().getReference("deliveryman/" + currentUserID);
        reservationReference.child("ride").removeValue();

        //remove the original request that has generated this ride
        DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference("deliveryman/" + currentUserID);
        requestReference.child("requests/" + ride.getOriginalRequestKey()).removeValue();

        //set this ride to free
        DatabaseReference deliverymanReference = FirebaseDatabase.getInstance().getReference("deliveryman/" + currentUserID);
        deliverymanReference.child("Busy").setValue(false);
        isRunning = false;


        DatabaseReference restaurantRef = FirebaseDatabase.getInstance().getReference("restaurants/" + ride.getRestaurantID()
                                            + "/reservations/" + ride.getOrderID());
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("customers/" + ride.getCustomerID()
                                            + "/reservations/" + ride.getOrderID());

        if(ride.getStatus() != RideStatus.BACKWARD){

            //notify success to both customer and restaurant
            restaurantRef.child("/status/en").setValue("Delivered");
            restaurantRef.child("/status/it").setValue("Consegnato");

            customerRef.child("/status/en").setValue("Delivered");
            customerRef.child("/status/it").setValue("Consegnato");
        }
        else{

            //notify failure to both customer and restaurant
            restaurantRef.child("/status/en").setValue("Failed");
            restaurantRef.child("/status/it").setValue("Fallito");

            customerRef.child("/status/en").setValue("Failed");
            customerRef.child("/status/it").setValue("Fallito");
        }

        ride = null;
        show_empty_view();

        /**
         * GO FROM CURRENT RIDE TO PENDING REQUESTS
         */
        Navigation.findNavController(fragView).navigate(R.id.action_ride_id_to_pendingReservations_id);
    }


    private class OnClickButtonMap implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String title = "",
                    message = "",
                    positive = "",
                    negative = "";

            final RideStatus curr_status = ride.getStatus();

            switch (curr_status){

                case TO_RESTAURANT:
                    //toward restaurant
                    title = getString(R.string.go_to_customer);
                    message = getString(R.string.dialog_go_to_customer);
                    positive = getString(R.string.yes);
                    negative = getString(R.string.no);
                    break;

                case TO_CUSTOMER:
                    //toward customer
                    title = getString(R.string.maps_button_order_delivered);
                    message = getString(R.string.dialog_message_order_outcome);
                    positive = getString(R.string.confirm);
                    negative = getString(R.string.quit_run);
                    break;

                case BACKWARD:
                    //failure: coming back to restaurant
                    title = getString(R.string.maps_button_order_failure);
                    message = getString(R.string.dialog_message_order_failure);
                    positive = getString(R.string.yes);
                    negative = getString(R.string.no);
                    break;

                default:
                    myToast.setText("STATUS ERROR");
                    myToast.show();
                    Log.d("matte", "STATUS ERROR: "+curr_status);
            }



            new AlertDialog.Builder(v.getContext())
                    .setTitle(title)
                    .setMessage(message)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            switch (curr_status){

                                case TO_RESTAURANT:
                                    to_customerDialog();
                                    break;

                                case TO_CUSTOMER:
                                    endDialog();
                                    break;

                                case BACKWARD:
                                    endDialog();
                                    break;

                                default:
                                    myToast.setText("STATUS ERROR");
                                    myToast.show();
                                    Log.d("matte", "STATUS ERROR: "+curr_status);
                            }
                        }
                    })

                    .setNegativeButton(negative, new DialogInterface.OnClickListener(){


                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (curr_status){

                                case TO_RESTAURANT:
                                    //do nothing: remain in the same state
                                    break;

                                case TO_CUSTOMER:
                                    //here quit the run: failure
                                    to_backwardDialog();
                                    break;

                                case BACKWARD:
                                    //do nothing: remain in the same state
                                    break;

                                default:
                                    myToast.setText("STATUS ERROR");
                                    myToast.show();
                                    Log.d("matte", "STATUS ERROR: "+curr_status);
                            }
                        }
                    })
                    .show();
        }
    }


    //go to customer
    private void to_customerDialog(){

        //set new status
        ride.setStatus(RideStatus.TO_CUSTOMER);
        isKmUploadNeeded = true; //need to upload the distance of the next leg (restaurant -> customer)
        if(hostActivity != null) //run until the distance is uploaded
            progressDialog = ProgressDialog.show(hostActivity, "", getString(R.string.loading));

        //show the new directions immediately based on the last detected position
        mMap.clear();
        if(oldLocation != null && customerPosition != null)
            showDirections(oldLocation, customerPosition, getString(R.string.customer_string));
        button_map.setText(getString(R.string.maps_button_order_delivered));

        //update the delivery status on FireBase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID+"/ride");
        reference.child("status").setValue(ride.getStatus().name());

        //inform customer the rider is coming
        sendNotificationToCustomer("The rider is coming!");

    }


    //end run
    private void endDialog(){

        myToast.setText(R.string.message_order_completed);
        myToast.show();

        //here returns and close this ride

        //retrieve actual time and terminate the order
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date date = new Date(); //initialized with current time
        String currentTime = dateFormat.format(date);
        terminateRide(currentTime);

    }


    //failure: coming back to restaurant
    private void to_backwardDialog(){

        //set new status
        ride.setStatus(RideStatus.BACKWARD);
        isKmUploadNeeded = true; //need to upload the distance of the next leg (customer -> restaurant)
        if(hostActivity != null) //run until the distance is uploaded
            progressDialog = ProgressDialog.show(hostActivity, "", getString(R.string.loading));

        //show the new directions immediately based on the last detected position
        mMap.clear();
        if(oldLocation != null && customerPosition != null)
            showDirections(oldLocation, restaurantPosition, getString(R.string.restaurant_string));
        button_map.setText(getString(R.string.maps_button_order_failure));

        //update the delivery status on FireBase
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID+"/ride");
        reference.child("status").setValue(ride.getStatus().name());

        //inform customer the rider is coming
        sendNotificationToRestaurant("Order " +ride.getOrderID() + " not delivered!");
        sendNotificationToCustomer("The rider has not found you at home! We suggest you to call the restaurant");

    }


    private void sendNotificationToCustomer(final String msg) {
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
                    send_email = ride.getCustomerID();

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

                                + "\"data\": {\"Order\": \"PolEATo\"},"
                                + "\"contents\": {\"en\": \"" + msg + "\"}"
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
    }


    private void sendNotificationToRestaurant(final String msg) {
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
                    send_email = ride.getRestaurantID();

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

                                + "\"data\": {\"Order\": \"PolEATo\"},"
                                + "\"contents\": {\"en\":" + msg + "\"+\"}"
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
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        deliveryReservationReference.removeAllListener();
        //delete the listener inside a try catch block because we cannot know if it was registered
        try{
            hostActivity.unregisterReceiver(locationReceiver); //TODO check if already registered
        }
        catch (Exception e){
            Log.d("matte", e.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //OnDestroy it is not called every time
        deliveryReservationReference.removeAllListener();
    }

    private void readOrder(DataSnapshot dataSnapshot){

        //retrieve order infos from DB
        String customerAddress = dataSnapshot.child("addressCustomer").getValue().toString();
        String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
        String numDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
        String orderID = dataSnapshot.child("orderID").getValue().toString();
        String nameCustomer = dataSnapshot.child("nameCustomer").getValue().toString();
        String customerID = dataSnapshot.child("CustomerID").getValue().toString();
        String restaurantID = dataSnapshot.child("restaurantID").getValue().toString();

        //replace comma with dot before change it in double
        String priceStr = dataSnapshot.child("totalPrice").getValue()
                .toString().replace(",", ".");

        //get the price with at leat 2 decimal digit
        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
        double d = Double.parseDouble(priceStr);
        priceStr = decimalFormat.format(d);

        String restaurantAddress = dataSnapshot.child("addressRestaurant").getValue().toString();
        String deliveryTime = dataSnapshot.child("deliveryTime").getValue().toString();
        String customerPhone = dataSnapshot.child("phoneCustomer").getValue().toString();
        String restaurantPhone = dataSnapshot.child("phoneRestaurant").getValue().toString();

        //time at which reservation notification arrived to the rider
        String startTime = dataSnapshot.child("startTime").getValue().toString();
        String statusStr = dataSnapshot.child("status").getValue().toString();
        String requestKey = dataSnapshot.child("requestKey").getValue().toString();

        //fill the fields
        tv_Fields.get("address").setText(customerAddress);
        tv_Fields.get("name").setText(nameCustomer);
        tv_Fields.get("restaurant").setText(nameRestaurant);
        tv_Fields.get("phone").setText(customerPhone);
        tv_Fields.get("dishes").setText(numDishes);
        tv_Fields.get("hour").setText(deliveryTime.split(" ")[1]);
        tv_Fields.get("price").setText(priceStr + "€");

        //the delivering value is not used in this phase
        ride = new Ride(orderID, customerAddress, restaurantAddress,
                nameCustomer, nameRestaurant, priceStr,
                numDishes, customerPhone, restaurantPhone,
                deliveryTime, customerID, restaurantID, startTime,
                RideStatus.valueOf(statusStr), requestKey,  null);

        //lock the rider
        isRunning = true;

        if(dataSnapshot.hasChild("totKm"))
            ride.addKm(Double.parseDouble(dataSnapshot.child("totKm").getValue().toString()));
        else {
            isKmUploadNeeded = true; //need to upload the distance of the first leg (current location -> restaurant)
            if(hostActivity != null) //start until the first distance is uploaded
                progressDialog = ProgressDialog.show(hostActivity, "", getString(R.string.loading));
        }

        //set the visibility
        show_order_view();

    }


    private void updateMapButton(){

        RideStatus curr_status = ride.getStatus();
        //set the button and status text based on the rider status
        String buttonText = "";
        switch (curr_status) {
            case TO_RESTAURANT:
                buttonText = getString(R.string.maps_button_to_restaurant);
                break;

            case TO_CUSTOMER:
                buttonText = getString(R.string.maps_button_order_delivered);
                break;

            case BACKWARD:
                buttonText = getString(R.string.maps_button_order_failure);
                break;

            default:
                myToast.setText("STATUS ERROR");
                myToast.show();
                Log.d("matte", "STATUS ERROR: "+curr_status);
        }

        button_map.setText(buttonText);

    }


    private void readTargetAddresses(){

        //retrieve location for customer and restaurant
        try {
            Address customerLocation = geocoder.getFromLocationName(ride.getAddressCustomer(), 1).get(0);
            customerPosition = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());

            Address restaurantLocation = geocoder.getFromLocationName(ride.getAddressRestaurant(), 1).get(0);
            restaurantPosition = new LatLng(restaurantLocation.getLatitude(), restaurantLocation.getLongitude());
        } catch (IOException e) {
            //if something goes wrong initialize randomly the position waiting for background service
            myToast.setText("Impossible to find latitude and longitude for addresses");
            myToast.show();
            customerPosition = new LatLng(0, 0);
            restaurantPosition = new LatLng(0, 0);
            e.printStackTrace();
        }
    }


    private BroadcastReceiver locationReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            Double latitude = b.getDouble("latitude");

            Double longitude = b.getDouble("longitude");

            Log.d("matte", "Received coordinates from service: " + latitude + " " + longitude);


            LatLng currLocation = new LatLng(latitude, longitude);

            if (oldLocation != null  && !isKmUploadNeeded) { //if the km upload is needed then call API anyway
                //check if the rider moved of at least 'X' m.
                double d = LocationUtilities.computeDistance(oldLocation, currLocation) * 1000;
                if (d <= MIN_DISTANCE_LOC_UPDATE)
                    return;
            }

            try {
                if (mMap != null) {
                    mMap.clear();
                    RideStatus curr_status = ride.getStatus();
                    switch (curr_status){

                        case TO_RESTAURANT:
                            showDirections(currLocation, restaurantPosition, getString(R.string.restaurant_string));
                            break;
                        case TO_CUSTOMER:
                            showDirections(currLocation, customerPosition, getString(R.string.customer_string));
                            break;

                        case BACKWARD:
                            showDirections(currLocation, restaurantPosition, getString(R.string.restaurant_string));
                            break;

                        default:
                            myToast.setText("STATUS ERROR");
                            myToast.show();
                            Log.d("matte", "STATUS ERROR: "+curr_status);
                    }
                }
                //update the last location
                oldLocation = currLocation;
            }catch (Exception e){
                Log.d("matte", e.getMessage());
            }
        }
    };


    private void moveCameraToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

    }


    //API request relating methods

    //uses the Google Direction API
    private void showDirections(LatLng origin, LatLng destination, String title){

        //add a marker for the destination
        mMap.addMarker(new MarkerOptions().position(destination)
                .title(title));
        //add customized marker for current position
        mMap.addMarker(new MarkerOptions().position(origin)
                .title(getString(R.string.you_string)))
                .setIcon(bitmapDescriptorFromVector(hostActivity, R.drawable.ic_baseline_directions_bike_24px));
        //move map camera
        moveCameraToCurrentLocation(origin);

        // check if already arrived
        double distance = LocationUtilities.computeDistance(origin, destination)*1000; //in meters
        if(distance < ARRIVED_DISTANCE){
            myToast.setText(getString(R.string.you_arrived));
            myToast.show();
            return;
        }

        // form: http://maps.googleapis.com/maps/api/directions/outputFormat?parameters
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+origin.latitude+","+origin.longitude+
                "&destination="+ destination.latitude+","+ destination.longitude+
                "&avoid=highways&mode=driving"+
                "&key="+getString(R.string.google_maps_key);

        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);

    }

    //to set distance and duration in the layout. It is called each time a new location is found
    private void setDirectionParameters(String distance, String duration){

        String s;
        try{
            tv_Fields.get("distance").setText(distance);
            tv_Fields.get("duration").setText(duration);
        }catch (Exception e){
            s = e.getMessage();
            Log.d("matte", s);
        }

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class FetchUrl extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String[] url) {
            // For storing data from web service
            Log.d("matte", url[0]);
            String data = "";
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String[] jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());
                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());
            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if(result == null)
                return;
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(hostActivity.getColor(R.color.colorPanelPrimary));
                Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
            //set current distance and duration updated
            setDirectionParameters(currDistance, currDuration);
        }
    }


    class DataParser {

        List<List<HashMap<String,String>>> parse(JSONObject jObject){
            List<List<HashMap<String, String>>> routes = new ArrayList<>() ;
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;
            try {
                jRoutes = jObject.getJSONArray("routes");
                /** Traversing all routes */
                for(int i=0;i<jRoutes.length();i++){
                    jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();
                    /** Traversing all legs */
                    for(int j=0;j<jLegs.length();j++){

                        //save the current distance and duration
                        currDistance = ( (JSONObject)jLegs.get(i)).getJSONObject("distance").get("text").toString();
                        currDuration = ( (JSONObject)jLegs.get(i)).getJSONObject("duration").get("text").toString();
                        //upload the total km for this order if needed
                        if(isKmUploadNeeded)
                            uploadKm(currDistance);

                        jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
                        /** Traversing all steps */
                        for(int k=0;k<jSteps.length();k++){
                            String polyline = "";
                            polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);
                            /** Traversing all points */
                            for(int l=0;l<list.size();l++){
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude) );
                                hm.put("lng", Double.toString((list.get(l)).longitude) );
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e){
            }

            return routes;
        }


        /**
         * Method to decode polyline points
         * */
        private List<LatLng> decodePoly(String encoded) {
            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;
                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }
            return poly;
        }
    }


    private void uploadKm(String distance){
        //distance = "2.43 km" -> take only the floating point number
        Double km = Double.parseDouble(distance.split(" ")[0]);
        ride.addKm(km);

        FirebaseDatabase.getInstance().getReference("deliveryman")
                                        .child(currentUserID+"/ride/totKm")
                                        .setValue(ride.getKm());
        isKmUploadNeeded = false;
        if(progressDialog.isShowing())
            progressDialog.dismiss();

    }

        /*private class OnClickRideStatus implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String title,
                    message;
            //towards restaurant
            if (!delivering) {
                title = getString(R.string.go_to_customer);
                message = getString(R.string.dialog_go_to_customer);
            } else {
                //toward customer
                title = getString(R.string.maps_button_order_delivered);
                message = getString(R.string.dialog_message_order_completed);
            }

            new AlertDialog.Builder(v.getContext())
                    .setTitle(title)
                    .setMessage(message)

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // If the rider arrives to the restaurant
                            if (!delivering) {
                                delivering = true;
                                button_map.setText(getString(R.string.maps_button_order_delivered));
                                //update the delivery status on FireBase
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deliveryman/" + currentUserID + "/reservations/"
                                        + rideKey);
                                reference.child("delivering").setValue(true);
                                sendNotificationToCustomer();
                            } else {
                                //here the order is completed
                                myToast.setText(R.string.message_order_completed);
                                myToast.show();

                                //retrieve actual time and terminate the order
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                Date date = new Date(); //initialized with current time
                                terminateRide(dateFormat.format(date));
                            }


                        }
                    })

                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }
    }*/

        /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        //Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.map_menu, menu);

        //If Button is visible go to DeliveringActivity
        menu.findItem(R.id.map_id).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent intent = new Intent(getActivity(), DeliveringActivity.class);
                intent.putExtra("ride", ride);
                intent.putExtra("order_key", rideKey);
                intent.putExtra("delivering", delivering);
                startActivityForResult(intent, MAPS_ACTIVITY_CODE);

                return true;
            }
        });

        // Used to set Visible Button map fo ActionBar
        if (show_icon_map_menu)
            menu.findItem(R.id.map_id).setVisible(true);
        else
            menu.findItem(R.id.map_id).setVisible(false);


        super.onCreateOptionsMenu(menu, inflater);
    }*/
}
