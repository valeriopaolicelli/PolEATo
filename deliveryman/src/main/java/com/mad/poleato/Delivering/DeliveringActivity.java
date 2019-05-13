package com.mad.poleato.Delivering;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.R;
import com.mad.poleato.Rides.Ride;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveringActivity extends FragmentActivity implements OnMapReadyCallback {

    private Toast myToast;

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    //two costant for frequency update tuning
    private static final int MIN_TIME_LOC_UPDATE = 5000;
    private static final int MIN_DISTANCE_LOC_UPDATE = 5;
    private static final double ARRIVED_DISTANCE = 8.0; //if closer to 8 meters than notify arrived

    //this flag is to indicate that the rider is already been at the restaurant
    private boolean toCustomer;

    //location data
    private GoogleMap mMap;
    private LatLng customerPosition;
    private LatLng restaurantPosition;
    private Geocoder geocoder;
    private LocationManager locationManager;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    //data structures for layout element storing
    private Map<String, TextView> tv_Fields;
    private Button button_map;

    //key of the order rider side
    private String reservationKey;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivering_layout);

        myToast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        geocoder = new Geocoder(this);

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //start the loading dialog
        //progressDialog = ProgressDialog.show(this, "", getString(R.string.loading));

        tv_Fields = new HashMap<>();
        //collects all the TextView inside the HashMap tv_Fields
        collectFields();

        //fill the fields with the Ride object from the Bundle
        fillFields();

        retrieveRideStatus();
    }

    private void fillFields(){

        Bundle bundle = getIntent().getExtras();
        Ride ride = (Ride) bundle.get("ride");
        reservationKey = (String) bundle.get("order_key");

        //fill the fields
        tv_Fields.get("address").setText(ride.getAddressCustomer());
        tv_Fields.get("name").setText(ride.getNameCustomer());
        tv_Fields.get("restaurant").setText(ride.getNameRestaurant());
        tv_Fields.get("phone").setText(ride.getPhoneCustomer());
        tv_Fields.get("dishes").setText(ride.getNumberOfDishes());
        tv_Fields.get("hour").setText(ride.getTime());
        tv_Fields.get("price").setText(ride.getTotalPrice()+"€");

        //retrieve location for customer and restaurant
        try {
            Address customerLocation = geocoder.getFromLocationName(ride.getAddressCustomer(), 1).get(0);
            customerPosition = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());

            Address restaurantLocation = geocoder.getFromLocationName(ride.getAddressRestaurant(), 1).get(0);
            restaurantPosition = new LatLng(restaurantLocation.getLatitude(), restaurantLocation.getLongitude());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        String buttonText;
        if(toCustomer){
            buttonText = getString(R.string.maps_button_order_delivered);
        }
        else{
            buttonText = getString(R.string.maps_button_to_restaurant);
        }

        //set the map button to display the right text based on the value of the delivering item on FireBase
        button_map = (Button) findViewById(R.id.button_map);
        button_map.setText(buttonText);
        button_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String title,
                        message;
                //towards restaurant
                if(!toCustomer){
                    title = getString(R.string.go_to_customer);
                    message = getString(R.string.dialog_go_to_customer);
                }
                else{
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
                                if(!toCustomer)
                                {
                                    toCustomer = true;
                                    @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                    button_map.setText(getString(R.string.maps_button_order_delivered));
                                    mMap.clear();
                                    if(lastKnownLocation != null)
                                    {
                                        LatLng lastPosition = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                        showDirections(lastPosition, customerPosition, getString(R.string.customer_string));
                                    }
                                    //update the delivery status
                                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID+"/reservations/"
                                            +reservationKey);
                                    reference.child("delivering").setValue(true);
                                }
                                else{
                                    myToast.setText(R.string.message_order_completed);
                                    myToast.show();

                                    //here returns and close this ride
                                    Intent returnIntent = new Intent();
                                    Date currentDate = Calendar.getInstance().getTime();
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                    String currentTime = sdf.format(currentDate);
                                    returnIntent.putExtra("deliveryHour", currentTime);
                                    Log.d("matte", "Delivering finished at "+currentTime);
                                    setResult(RESULT_OK,returnIntent);
                                    finish();

                                }


                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        mapFragment.getMapAsync(this);
    }


    private void retrieveRideStatus() {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("deliveryman/" + currentUserID + "/reservations");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("phoneCustomer") &&
                        dataSnapshot.hasChild("phoneRestaurant") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("delivering")){

                    toCustomer = (Boolean)dataSnapshot.child("delivering").getValue();

                    /*try {
                        Address customerLocation = geocoder.getFromLocationName(customerAddress, 1).get(0);
                        customerPosition = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());

                        Address restaurantLocation = geocoder.getFromLocationName(restaurantAddress, 1).get(0);
                        restaurantPosition = new LatLng(restaurantLocation.getLatitude(), restaurantLocation.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    createMap();

                }

                /*if (dataSnapshot.exists() &&
                        !isRunning &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("phoneCustomer") &&
                        dataSnapshot.hasChild("phoneRestaurant") &&
                        dataSnapshot.hasChild("time")) {

                    //retrieve order infos from DB
                    String customerAddress = dataSnapshot.child("addressCustomer").getValue().toString();
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                    String numDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
                    String orderID = dataSnapshot.child("orderID").getValue().toString();
                    String nameCustomer = dataSnapshot.child("nameCustomer").getValue().toString();
                    String priceStr = dataSnapshot.child("totalPrice").getValue()
                                            .toString().replace(",", ".");
                    String restaurantAddress = dataSnapshot.child("addressRestaurant").getValue().toString();
                    String deliveryTime = dataSnapshot.child("time").getValue().toString();
                    String customerPhone = dataSnapshot.child("phoneCustomer").getValue().toString();

                    //fill the fields
                    tv_Fields.get("address").setText(customerAddress);
                    tv_Fields.get("name").setText(nameCustomer);
                    tv_Fields.get("restaurant").setText(nameRestaurant);
                    tv_Fields.get("phone").setText(customerPhone);
                    tv_Fields.get("dishes").setText(numDishes);
                    tv_Fields.get("hour").setText(deliveryTime);
                    tv_Fields.get("price").setText(priceStr+"€");

                    try {
                        Address customerLocation = geocoder.getFromLocationName(customerAddress, 1).get(0);
                        customerPosition = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());

                        Address restaurantLocation = geocoder.getFromLocationName(restaurantAddress, 1).get(0);
                        restaurantPosition = new LatLng(restaurantLocation.getLatitude(), restaurantLocation.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isRunning = true;
                    createMap();
                }*/
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("phoneCustomer") &&
                        dataSnapshot.hasChild("phoneRestaurant") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("delivering")){

                    toCustomer = (Boolean)dataSnapshot.child("delivering").getValue();

                    /*try {
                        Address customerLocation = geocoder.getFromLocationName(customerAddress, 1).get(0);
                        customerPosition = new LatLng(customerLocation.getLatitude(), customerLocation.getLongitude());

                        Address restaurantLocation = geocoder.getFromLocationName(restaurantAddress, 1).get(0);
                        restaurantPosition = new LatLng(restaurantLocation.getLatitude(), restaurantLocation.getLongitude());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    createMap();

                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //an order cannot be removed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //an order cannot be moved
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void collectFields() {

        tv_Fields.put("address", (TextView)findViewById(R.id.deliveryAddress_tv));
        tv_Fields.put("name", (TextView)findViewById(R.id.customerName_tv));
        tv_Fields.put("restaurant", (TextView)findViewById(R.id.restaurant_tv));
        tv_Fields.put("phone", (TextView)findViewById(R.id.phone_tv));
        tv_Fields.put("dishes", (TextView)findViewById(R.id.dishes_tv));
        tv_Fields.put("hour", (TextView)findViewById(R.id.time_tv));
        tv_Fields.put("price", (TextView)findViewById(R.id.cost_tv));

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLocation));
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setBuildingsEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);

            return;
        }

        //the first time the location updates would take times, so we retrieve once the last known location
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastKnownLocation == null){

            Log.d("matte", "GPS returns null, using Network for last known location");
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        }

        if(lastKnownLocation != null){

            LatLng lastKnownLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            if(toCustomer)
                showDirections(lastKnownLatLng, customerPosition, getString(R.string.customer_string));
            else
                showDirections(lastKnownLatLng, restaurantPosition, getString(R.string.restaurant_string));
        }
        else
            Log.d("matte", "Last location not found");

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_LOC_UPDATE,
                MIN_DISTANCE_LOC_UPDATE, new LocListener());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("matte", "code 0 granted");

                } else {

                    Log.d("matte", "code 0 not granted");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("matte", "code 1 granted");

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.7
                    Log.d("matte", "code 1 not granted");
                }
                return;
            }
        }

    }

    // Define a listener that responds to location updates
    class LocListener implements LocationListener {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            Log.d("matte", "Location changed: "+location.getLatitude()+", "+location.getLongitude());
            mMap.clear();
            if(toCustomer)
                showDirections(new LatLng(location.getLatitude(), location.getLongitude()), customerPosition, getString(R.string.customer_string));
            else
                showDirections(new LatLng(location.getLatitude(), location.getLongitude()), restaurantPosition, getString(R.string.restaurant_string));
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };


    public double computeDistance(LatLng origin, LatLng destination) {

        double startLat,
                endLat;
        double dLat  = Math.toRadians((destination.latitude - origin.latitude));
        double dLong = Math.toRadians((destination.longitude - origin.longitude));

        startLat = Math.toRadians(origin.latitude);
        endLat   = Math.toRadians(destination.longitude);

        double a = haversin(dLat) + Math.cos(origin.latitude) * Math.cos(destination.latitude) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;
        return distance;
    }


    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }


    //uses the Google Direction API
    private void showDirections(LatLng origin, LatLng destination, String title){

        //add a marker for the destination
        mMap.addMarker(new MarkerOptions().position(destination)
                .title(title));

        //add customized marker for current position
        mMap.addMarker(new MarkerOptions().position(origin)
                .title(getString(R.string.you_string)))
                .setIcon(bitmapDescriptorFromVector(this, R.drawable.ic_baseline_directions_bike_24px));
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

        // check if already arrived
        double distance = computeDistance(origin, destination)*1000; //in meters

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
                Log.d("ParserTask",jsonData[0].toString());
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
                lineOptions.color(getColor(R.color.colorPanelPrimary));
                Log.d("onPostExecute","onPostExecute lineoptions decoded");
            }
            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
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




}
