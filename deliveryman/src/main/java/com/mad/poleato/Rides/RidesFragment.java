package com.mad.poleato.Rides;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.Delivering.DeliveringActivity;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class RidesFragment extends Fragment {

    private Toast myToast;

    private final static int MAPS_ACTIVITY_CODE = 1; //code for startActivityForResult

    private Activity hostActivity;
    private View fragView;

    private Map<String, TextView> tv_Fields;
    private FloatingActionButton map_button;

    //to set the visibility to gone when there are no ride available
    private CardView cardview;
    private FrameLayout frameLayout;
    private TextView emptyView;

    //the key for that order at rider side
    private String orderKey;

    //this flag is to avoid multiple order that will override the maps
    private boolean isRunning;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    private Ride ride;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if (hostActivity != null) {
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        //initially the rider is free from work
        isRunning = false;

        tv_Fields = new HashMap<>();

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.popup_account_settings, menu);
        menu.findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //logout
                Log.d("matte", "Logout");
                FirebaseAuth.getInstance().signOut();
                //                OneSignal.sendTag("User_ID", "");
                OneSignal.setSubscription(false);

                /**
                 *  GO TO LOGIN ****
                 */
                Navigation.findNavController(fragView).navigate(R.id.action_rides_id_to_signInActivity);
                getActivity().finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.ride_layout, container, false);

        //collect the TextView inside the map
        collectFields();

        //download the ride data from firebase
        retrieveOrderInfo();

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    private void collectFields() {

        tv_Fields.put("address", (TextView) fragView.findViewById(R.id.deliveryAddress_tv));
        tv_Fields.put("name", (TextView) fragView.findViewById(R.id.customerName_tv));
        tv_Fields.put("restaurant", (TextView) fragView.findViewById(R.id.restaurant_tv));
        tv_Fields.put("phone", (TextView) fragView.findViewById(R.id.phone_tv));
        tv_Fields.put("dishes", (TextView) fragView.findViewById(R.id.dishes_tv));
        tv_Fields.put("hour", (TextView) fragView.findViewById(R.id.time_tv));
        tv_Fields.put("price", (TextView) fragView.findViewById(R.id.cost_tv));

        cardview = (CardView) fragView.findViewById(R.id.rideCardView);
        frameLayout = (FrameLayout) fragView.findViewById(R.id.rideFrameLayout);
        emptyView = (TextView) fragView.findViewById(R.id.ride_empty_view);

        map_button = (FloatingActionButton) fragView.findViewById(R.id.map_button);
        map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeliveringActivity.class);
                intent.putExtra("ride", ride);
                intent.putExtra("order_key", orderKey);
                startActivityForResult(intent, MAPS_ACTIVITY_CODE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MAPS_ACTIVITY_CODE){
            if(resultCode == RESULT_OK){
                //retrieve the delivered hour
                Bundle b = data.getExtras();
                String deliveredHour = b.get("deliveryHour").toString();

                //save the completed ride into the history
                DatabaseReference historyReference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID+"/history").push();
                historyReference.child("orderID").setValue(ride.getOrderID());
                historyReference.child("addressRestaurant").setValue(ride.getAddressRestaurant());
                historyReference.child("nameRestaurant").setValue(ride.getNameRestaurant());
                historyReference.child("totalPrice").setValue(ride.getTotalPrice());
                historyReference.child("numberOfDishes").setValue(ride.getNumberOfDishes());
                historyReference.child("expectedTime").setValue(ride.getTime());
                historyReference.child("deliveredTime").setValue(deliveredHour);

                //remove the ride
                DatabaseReference reservationReference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID);
                reservationReference.child("reservations").removeValue();

                //set this ride to free
                DatabaseReference deliverymanReference = FirebaseDatabase.getInstance().getReference("deliveryman/"+currentUserID);
                deliverymanReference.child("Busy").setValue(false);
                isRunning = false;

            }
            /*else if(resultCode == RESULT_CANCELED){


            }*/
        }
    }

    private void retrieveOrderInfo() {

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("deliveryman/" + currentUserID + "/reservations");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists() &&
                        !isRunning &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("CustomerID") &&
                        dataSnapshot.hasChild("delivering") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("totalPrice") &&
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
                    String restaurantPhone = dataSnapshot.child("phoneRestaurant").getValue().toString();

                    orderKey = dataSnapshot.getKey();

                    //fill the fields
                    tv_Fields.get("address").setText(customerAddress);
                    tv_Fields.get("name").setText(nameCustomer);
                    tv_Fields.get("restaurant").setText(nameRestaurant);
                    tv_Fields.get("phone").setText(customerPhone);
                    tv_Fields.get("dishes").setText(numDishes);
                    tv_Fields.get("hour").setText(deliveryTime);
                    tv_Fields.get("price").setText(priceStr+"€");

                    ride = new Ride(orderID, customerAddress, restaurantAddress,
                            nameCustomer, nameRestaurant, priceStr,
                            numDishes, customerPhone, restaurantPhone,
                            deliveryTime);

                    //lock the rider
                    isRunning = true;

                    //set the visibility
                    emptyView.setVisibility(View.GONE);
                    cardview.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //an order cannot change
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //the order can be removed after its completion
                ride = null;
                emptyView.setVisibility(View.VISIBLE);
                cardview.setVisibility(View.GONE);
                frameLayout.setVisibility(View.GONE);
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

}
