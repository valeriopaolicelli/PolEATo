package com.mad.poleato.PendingRequests;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.AuthentucatorD.Authenticator;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.R;
import com.mad.poleato.Ride.Ride;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    private Toast myToast;
    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    private View fragView;
    private Activity hostActivity;

    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
    private RequestsRecyclerViewAdapter requestsAdapter;
    private ImageView empty_view;

    private HashMap<String, MyDatabaseReference> referenceMap;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if (hostActivity != null) {
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_SHORT);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

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

        referenceMap = new HashMap<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView =  inflater.inflate(R.layout.pending_requests_recycler, container, false);

        /** Logout a priori if access is revoked */
        if(currentUserID == null)
            Authenticator.revokeAccess(Objects.requireNonNull(getActivity()), fragView);

        empty_view = (ImageView) fragView.findViewById(R.id.requests_empty_view);
        rv = (RecyclerView) fragView.findViewById(R.id.requests_recyler);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(hostActivity);
        rv.setLayoutManager(layoutManager);

        this.requestsAdapter = new RequestsRecyclerViewAdapter(getContext(), currentUserID, myToast);
        rv.setAdapter(requestsAdapter);
        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        show_empty_view();
        attachFirebaseListeners();

        return fragView;
    }

    private void show_empty_view(){

        rv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    private void show_requests_view(){

        empty_view.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
    }

    private void attachFirebaseListeners(){

        //listen for isActive and busy values changes

        referenceMap.put("currentRider", new MyDatabaseReference(FirebaseDatabase.getInstance()
                                                            .getReference("deliveryman/"+currentUserID)));
        referenceMap.get("currentRider").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("Busy") && dataSnapshot.hasChild("IsActive")){
                        Boolean busy =  (Boolean) dataSnapshot.child("Busy").getValue();
                        requestsAdapter.setBusy(busy);
                        Boolean isActive= (Boolean) dataSnapshot.child("IsActive").getValue();
                        requestsAdapter.setIsActive(isActive);
                    }else{
                        Log.d("matte", "[ERROR] Busy and isActive values not found on DB!!");
                        myToast.setText("ERROR WITH BUSY VALUE FB");
                        myToast.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        /*referenceMap.put("busy", new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("deliveryman")
                                                                .child(currentUserID+"/Busy")));
        referenceMap.get("busy").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    Boolean busy =  (Boolean) dataSnapshot.getValue();
                    requestsAdapter.setBusy(busy);
                }
                else{
                    Log.d("matte", "[ERROR] Busy value not found on DB!!");
                    myToast.setText("ERROR WITH BUSY VALUE FB");
                    myToast.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/
        //listen for requests list
        referenceMap.put("requests", new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("deliveryman")
                                                                    .child(currentUserID+"/requests")));
        referenceMap.get("requests").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists() &&
                    dataSnapshot.hasChild("CustomerID") &&
                    dataSnapshot.hasChild("addressCustomer") &&
                    dataSnapshot.hasChild("addressRestaurant") &&
                    dataSnapshot.hasChild("deliveryTime") &&
                    dataSnapshot.hasChild("nameCustomer") &&
                    dataSnapshot.hasChild("nameRestaurant") &&
                    dataSnapshot.hasChild("numberOfDishes") &&
                    dataSnapshot.hasChild("orderID") &&
                    dataSnapshot.hasChild("phoneCustomer") &&
                    dataSnapshot.hasChild("phoneRestaurant") &&
                    dataSnapshot.hasChild("restaurantID") &&
                    dataSnapshot.hasChild("totalPrice") &&
                    dataSnapshot.hasChild("delivering")){

                    //create the requests and add it to the recyclerView
                    createRequest(dataSnapshot);
                    if(requestsAdapter.getItemCount() == 1) //previously it was 0
                        show_requests_view();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists() &&
                        dataSnapshot.hasChild("CustomerID") &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("deliveryTime") &&
                        dataSnapshot.hasChild("nameCustomer") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("phoneCustomer") &&
                        dataSnapshot.hasChild("phoneRestaurant") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("delivering")){

                    //create the requests and add it to the recyclerView
                    createRequest(dataSnapshot);
                    if(requestsAdapter.getItemCount() == 1) //previously it was 0
                        show_requests_view();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                String orderID = dataSnapshot.child("orderID").getValue().toString();
                String addressCustomer = dataSnapshot.child("addressCustomer").getValue().toString();
                String addressRestaurant = dataSnapshot.child("addressRestaurant").getValue().toString();
                String nameCustomer = dataSnapshot.child("nameCustomer").getValue().toString();
                String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                String totalPrice = dataSnapshot.child("totalPrice").getValue().toString();
                String numberOfDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
                String phoneCustomer = dataSnapshot.child("phoneCustomer").getValue().toString();
                String phoneRestaurant = dataSnapshot.child("phoneRestaurant").getValue().toString();
                String deliveryTime = dataSnapshot.child("deliveryTime").getValue().toString();
                String customerID = dataSnapshot.child("CustomerID").getValue().toString();
                String restaurantID = dataSnapshot.child("restaurantID").getValue().toString();
                Boolean delivering = (Boolean) dataSnapshot.child("delivering").getValue();
                String requestKey = dataSnapshot.getKey();


                //startTime and status are null in this phase
                Ride r = new Ride(orderID, addressCustomer, addressRestaurant, nameCustomer,
                        nameRestaurant, totalPrice, numberOfDishes, phoneCustomer,
                        phoneRestaurant, deliveryTime, customerID, restaurantID,
                        null, null, requestKey, delivering);

                requestsAdapter.removeRequest(r);
                if(requestsAdapter.getItemCount() == 0)
                    show_empty_view();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createRequest(DataSnapshot snap){

        String orderID = snap.child("orderID").getValue().toString();
        String addressCustomer = snap.child("addressCustomer").getValue().toString();
        String addressRestaurant = snap.child("addressRestaurant").getValue().toString();
        String nameCustomer = snap.child("nameCustomer").getValue().toString();
        String nameRestaurant = snap.child("nameRestaurant").getValue().toString();
        String totalPrice = snap.child("totalPrice").getValue().toString();
        String numberOfDishes = snap.child("numberOfDishes").getValue().toString();
        String phoneCustomer = snap.child("phoneCustomer").getValue().toString();
        String phoneRestaurant = snap.child("phoneRestaurant").getValue().toString();
        String deliveryTime = snap.child("deliveryTime").getValue().toString();
        String customerID = snap.child("CustomerID").getValue().toString();
        String restaurantID = snap.child("restaurantID").getValue().toString();
        Boolean delivering = (Boolean) snap.child("delivering").getValue();
        String requestKey = snap.getKey();


        //startTime and status are null in this phase
        Ride r = new Ride(orderID, addressCustomer, addressRestaurant, nameCustomer,
                nameRestaurant, totalPrice, numberOfDishes, phoneCustomer,
                phoneRestaurant, deliveryTime, customerID, restaurantID,
                null, null, requestKey, delivering);

        requestsAdapter.addRequest(r);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //remove all the active listeners
        for(MyDatabaseReference ref : referenceMap.values())
            ref.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        //OnDestroy it is not called every time
        for(MyDatabaseReference ref : referenceMap.values())
            ref.removeAllListener();
    }

}
