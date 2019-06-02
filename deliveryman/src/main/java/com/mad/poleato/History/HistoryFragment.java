package com.mad.poleato.History;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;


/**
 * This is the fragment to show the history of the Rider
 */
public class HistoryFragment extends Fragment {

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    private Toast myToast;

    private Activity hostActivity;
    private View fragView;
    private HistoryRecyclerViewAdapter historyAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
    private ImageView empty_view;

    private ProgressDialog progressDialog;
    private MyDatabaseReference historyReference;

    private List<HistoryItem> historyItemList;



    public HistoryFragment() {
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

        if (getActivity() != null) {
            progressDialog = ProgressDialog.show(getActivity(), "", hostActivity.getString(R.string.loading));
        }

        historyItemList = new ArrayList<>();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.history_recyclerview, container, false);


        empty_view = (ImageView) fragView.findViewById(R.id.history_empty_view);
        rv = (RecyclerView) fragView.findViewById(R.id.history_recyclerview);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.historyAdapter = new HistoryRecyclerViewAdapter(this.hostActivity);
        rv.setAdapter(historyAdapter);
        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        show_empty_view();
        attachFirebaseListeners();

        return fragView;
    }


    /**
     * This is the method to hide the main view and show the hidden one
     */
    private void show_empty_view(){

        rv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    /**
     * This is the method to hide the empty view and show the main one
     */
    private void show_history_view(){

        empty_view.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
    }

    /**
     * This method attaches the firebase listeners to download the history
     */
    private void attachFirebaseListeners(){

        historyReference = new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("deliveryman")
                                                            .child(currentUserID).child("history"));
        historyReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Log.d("matte", "[ERROR] dataSnapshot does not exist");
                }

                //the ValueEventListener will be called after all the ChildEvenentListener
                historyAdapter.setAllHistories(historyItemList);

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        historyReference.setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists() &&
                    dataSnapshot.hasChild("addressRestaurant") &&
                    dataSnapshot.hasChild("endTime") &&
                    dataSnapshot.hasChild("expectedTime") &&
                    dataSnapshot.hasChild("nameRestaurant") &&
                    dataSnapshot.hasChild("numberOfDishes") &&
                    dataSnapshot.hasChild("orderID") &&
                    dataSnapshot.hasChild("outcome") &&
                    dataSnapshot.hasChild("startTime") &&
                    dataSnapshot.hasChild("totKm") &&
                    dataSnapshot.hasChild("totalPrice")){

                    //retrieve history infos from DB
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                    String numDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
                    String orderID = dataSnapshot.child("orderID").getValue().toString();
                    String priceStr = dataSnapshot.child("totalPrice").getValue()
                            .toString().replace(",", ".");
                    String restaurantAddress = dataSnapshot.child("addressRestaurant").getValue().toString();
                    String expectedTime = dataSnapshot.child("expectedTime").getValue().toString();
                    String outcome = dataSnapshot.child("outcome").getValue().toString();
                    String deliveredTime = dataSnapshot.child("endTime").getValue().toString();


                    HistoryItem historyObj = new HistoryItem(orderID, restaurantAddress, nameRestaurant,
                            priceStr, numDishes, expectedTime,
                            deliveredTime, HistoryItemOutcome.valueOf(outcome));

                    historyItemList.add(historyObj);
                    show_history_view();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.exists() &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("endTime") &&
                        dataSnapshot.hasChild("expectedTime") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("outcome") &&
                        dataSnapshot.hasChild("startTime") &&
                        dataSnapshot.hasChild("totKm") &&
                        dataSnapshot.hasChild("totalPrice")){

                    //retrieve history infos from DB
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                    String numDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
                    String orderID = dataSnapshot.child("orderID").getValue().toString();
                    String priceStr = dataSnapshot.child("totalPrice").getValue()
                            .toString().replace(",", ".");
                    String restaurantAddress = dataSnapshot.child("addressRestaurant").getValue().toString();
                    String expectedTime = dataSnapshot.child("expectedTime").getValue().toString();
                    String outcome = dataSnapshot.child("outcome").getValue().toString();
                    String deliveredTime = dataSnapshot.child("endTime").getValue().toString();


                    HistoryItem historyObj = new HistoryItem(orderID, restaurantAddress, nameRestaurant,
                            priceStr, numDishes, expectedTime,
                            deliveredTime, HistoryItemOutcome.valueOf(outcome));

                    historyItemList.add(historyObj);
                    show_history_view();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //history item cannot be removed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    @Override
    public void onStop() {
        super.onStop();
        //OnDestroy it is not called every time
       historyReference.removeAllListener();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        historyReference.removeAllListener();
    }
}
