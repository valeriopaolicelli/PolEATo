package com.mad.poleato.Delivering;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveringActivity extends FragmentActivity implements OnMapReadyCallback {

    //this flag is to avoid multiple order that will override the maps
    private boolean isRunning;

    private GoogleMap mMap;
    private Address CustomerLocation;
    private Geocoder geocoder;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    //loading bar
    private ProgressDialog progressDialog;

    private Map<String, TextView> tv_Fields;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delivering_layout);

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        //initially this rider is available
        isRunning = false;

        geocoder = new Geocoder(this);

        //start the loading dialog
        progressDialog = ProgressDialog.show(this, "", getString(R.string.loading));

        tv_Fields = new HashMap<>();
        //collects all the TextView inside the HashMap tv_Fields
        collectFields();


        retrieveOrderInfo();
    }

    private void createMap(){

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private void retrieveOrderInfo(){

        DatabaseReference reference = FirebaseDatabase.getInstance()
                                        .getReference("deliveryman/"+currentUserID+"/reservations");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists() &&
                    !isRunning &&
                    dataSnapshot.hasChild("addressCustomer") &&
                    dataSnapshot.hasChild("addressRestaurant") &&
                    dataSnapshot.hasChild("customerID") &&
                    dataSnapshot.hasChild("nameRestaurant") &&
                    dataSnapshot.hasChild("numberOfDishes") &&
                    dataSnapshot.hasChild("orderID") &&
                    dataSnapshot.hasChild("surnameCustomer") &&
                    dataSnapshot.hasChild("totalPrice")){

                    String customerAddress = dataSnapshot.child("addressCustomer").toString();

                    try {
                        CustomerLocation = geocoder.getFromLocationName(customerAddress, 1).get(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isRunning = true;
                    createMap();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //an order cannot change
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



    private void collectFields(){

        tv_Fields.put("order_tv", (TextView)findViewById(R.id.order_tv));
        tv_Fields.put("surname_tv", (TextView)findViewById(R.id.surname_tv));
        tv_Fields.put("deliveryAddress_tv", (TextView)findViewById(R.id.deliveryAddress_tv));
        tv_Fields.put("restaurant_tv", (TextView)findViewById(R.id.restaurant_tv));
        tv_Fields.put("restaurantAddress_tv", (TextView)findViewById(R.id.restaurantAddress_tv));
        tv_Fields.put("totalPrice_tv", (TextView)findViewById(R.id.totalPrice_tv));
        tv_Fields.put("dishes_tv", (TextView)findViewById(R.id.dishes_tv));

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same CustomerLocation.

        mMap = googleMap;
        LatLng customerLocation = new LatLng(CustomerLocation.getLatitude(), CustomerLocation.getLongitude());
        mMap.addMarker(new MarkerOptions().position(customerLocation)
                .title("Customer"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(customerLocation, 15.0f));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(customerLocation));
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setBuildingsEnabled(true);

        // Getting URL to the Google Directions API
        LatLng origin = new LatLng(45.050938, 7.649423);

        // form: http://maps.googleapis.com/maps/api/directions/outputFormat?parameters
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+origin.latitude+","+origin.longitude+
                        "&destination="+customerLocation.latitude+","+customerLocation.longitude+
                        "&avoid=highways&mode=driving"+
                        "&key="+getString(R.string.google_maps_key);

        /*String url = "https://www.google.com/maps/dir/?api=1&origin="+origin.latitude+"%2C"+origin.longitude+
                "&destination="+customerLocation.latitude+"%2C"+customerLocation.longitude+"&travelmode=driving";*/

        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));

        if(progressDialog.isShowing())
            progressDialog.dismiss();

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
