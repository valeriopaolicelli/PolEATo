package com.mad.poleato.Rides;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.LocationReceiver;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DeliveringActivity extends FragmentActivity implements OnMapReadyCallback {

    //code for notify permission denied (visible also to RidesFragment)
    protected final static int RESULT_PERMISSION_DENIED = 10001;
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    //two costant for frequency update tuning
    private static final int MIN_TIME_LOC_UPDATE = 5000;
    private static final int MIN_DISTANCE_LOC_UPDATE = 7;
    private static final double ARRIVED_DISTANCE = 8.0; //if closer to 8 meters than notify arrived


    private Toast myToast;

    /*It correspond with the ride status: if the order is delivering or is still at restaurant
        based on that value the map will provide directions towards a certain target (restaurant or customer)*/
    private boolean delivering;
    private Ride ride;

    //location data
    private GoogleMap mMap;
    private LatLng customerPosition;
    private LatLng restaurantPosition;
    private Geocoder geocoder;
    private LocationManager locationManager;

    //current distance and duration of the ride
    private String currDistance;
    private String currDuration;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    //data structures for layout element storing
    private Map<String, TextView> tv_Fields;
    private ImageButton show_more_button;
    private Button button_map;

    /*the latest location received from the backgound service. It is needed to check if the rider is moving
        if not the directions are not updated each time */
    private LatLng oldLocation;

    //key of the order rider side
    private String reservationKey;




    BroadcastReceiver locationReceiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle b = intent.getExtras();

            Double latitude = b.getDouble("latitude");

            Double longitude = b.getDouble("longitude");

            Log.d("matte", "Received coordinates from service: " + latitude + " " + longitude);


            LatLng currLocation = new LatLng(latitude, longitude);

            if (oldLocation != null) {
                //check if the rider moved of at least 'X' m.
                double d = computeDistance(oldLocation, currLocation) * 1000;
                if (d <= MIN_DISTANCE_LOC_UPDATE)
                    return;
            }

            try {
                if (mMap != null) {
                    mMap.clear();
                    if (delivering)
                        showDirections(currLocation, customerPosition, getString(R.string.customer_string));
                    else
                        showDirections(currLocation, restaurantPosition, getString(R.string.restaurant_string));
                }
                //update the last location
                oldLocation = currLocation;
            }catch (Exception e){
                Log.d("matte", e.getMessage());
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivering_layout);

        myToast = Toast.makeText(this, "", Toast.LENGTH_LONG);

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        //to retrieve latitude and longitude from address
        geocoder = new Geocoder(this);


        tv_Fields = new HashMap<>();
        //collects all the TextView inside the HashMap tv_Fields
        collectFields();

        //fill the fields with the Ride object from the Bundle
        fillFields();

        createMap();

    }


    @Override
    protected void onDestroy() {
        //delete the listener
        unregisterReceiver(locationReceiver);
        super.onDestroy();
    }


    private void collectFields() {

        tv_Fields.put("address", (TextView)findViewById(R.id.deliveryAddress_tv));
        tv_Fields.put("name", (TextView)findViewById(R.id.customerName_tv));
        tv_Fields.put("restaurant", (TextView)findViewById(R.id.restaurant_tv));
        tv_Fields.put("phone", (TextView)findViewById(R.id.phone_tv));
        tv_Fields.put("dishes", (TextView)findViewById(R.id.dishes_tv));
        tv_Fields.put("hour", (TextView)findViewById(R.id.time_tv));
        tv_Fields.put("price", (TextView)findViewById(R.id.cost_tv));
        tv_Fields.put("distance", (TextView)findViewById(R.id.distance_tv));
        tv_Fields.put("duration", (TextView)findViewById(R.id.duration_tv));

        show_more_button = (ImageButton) findViewById(R.id.showMoreButton);

    }


    private void fillFields(){
        //retrieve the reservation data from bundle
        Bundle bundle = getIntent().getExtras();
        ride = (Ride) bundle.get("ride");
        reservationKey = (String) bundle.get("order_key"); //reservation key rider side
        delivering = (Boolean) bundle.get("delivering"); //ride status

        //fill the fields
        tv_Fields.get("address").setText(ride.getAddressCustomer());
        tv_Fields.get("name").setText(ride.getNameCustomer());
        tv_Fields.get("restaurant").setText(ride.getNameRestaurant());
        tv_Fields.get("phone").setText(ride.getPhoneCustomer());
        tv_Fields.get("dishes").setText(ride.getNumberOfDishes());
        tv_Fields.get("hour").setText(ride.getTime());
        tv_Fields.get("price").setText(ride.getTotalPrice()+"€");

        show_more_button.setOnClickListener(new OnClickShowMore());

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

        // Acquire a reference to the system Location Manager
        registerReceiver(locationReceiver,new IntentFilter("Coordinates"));

    }


    public class OnClickShowMore implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("show_more_fragment");
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
            showMoreFrag.show(ft, "show_more_fragment");
        }
    }


    private void createMap() {

        //get the map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //set the button text based on the rider status
        String buttonText;
        if(delivering){
            buttonText = getString(R.string.maps_button_order_delivered);
        }
        else{
            buttonText = getString(R.string.maps_button_to_restaurant);
        }

        //set the map button to display the right text based on the value of the delivering item on FireBase
        button_map = (Button) findViewById(R.id.button_map);
        button_map.setText(buttonText);
        button_map.setOnClickListener(new OnClickButtonMap());

        //when the map is ready the callback method OnMapReady will be called
        mapFragment.getMapAsync(this);
    }

    private class OnClickButtonMap implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String title,
                    message;
            //towards restaurant
            if(!delivering){
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
                            if(!delivering)
                            {
                                delivering = true;

                                //show the new directions immediately based on the last detected position
                                if(oldLocation != null && customerPosition != null)
                                    showDirections(oldLocation, restaurantPosition, getString(R.string.customer_string));
                                button_map.setText(getString(R.string.maps_button_order_delivered));
                                mMap.clear();
                                //update the delivery status on FireBase
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID+"/reservations/"
                                        +reservationKey);
                                reference.child("delivering").setValue(true);
                                sendNotificationToCustomer();
                            }
                            else{
                                myToast.setText(R.string.message_order_completed);
                                myToast.show();

                                //here returns and close this ride
                                Intent returnIntent = new Intent();

                                /*Date currentDate = Calendar.getInstance().getTime();
                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                String currentTime = sdf.format(currentDate);*/
                                //retrieve actual time and terminate the order
                                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                Date date = new Date();
                                String currentTime = dateFormat.format(date);

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
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        //check if permission for location if permission for location was grant
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
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
                    //permission denied, return error
                    Intent returnIntent = new Intent();
                    setResult(RESULT_PERMISSION_DENIED,returnIntent);
                    finish();
                }
                return;
            }
        }

    }


    public double computeDistance(LatLng origin, LatLng destination) {

        double dLat  = Math.toRadians((destination.latitude - origin.latitude));
        double dLong = Math.toRadians((destination.longitude - origin.longitude));

        double a = haversin(dLat) + Math.cos(origin.latitude) * Math.cos(destination.latitude) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;
        return distance;
    }


    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }


    private void moveCameraToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

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
        moveCameraToCurrentLocation(origin);

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

    private void sendNotificationToCustomer() {
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
                    send_email= ride.getCustomerID();

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
                                + "\"contents\": {\"en\": \"Il fattorino ha lasciato il ristorante\"}"
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

}
