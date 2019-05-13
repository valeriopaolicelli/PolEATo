package com.mad.poleato.FirebaseData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.History.HistoryItem;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.util.List;
import java.util.logging.Handler;

public class MyFirebaseData {

    private MyViewModel model;
    private Activity activity;
    private Handler handler;
    private ProgressDialog progressDialog;

    private String currentUserID;
    private FirebaseAuth mAuth;

    public MyFirebaseData(Activity activity, ProgressDialog progressDialog) {
        this.activity = activity;
        this.progressDialog = progressDialog;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
    }

    public void fillFieldsHistory() {

        model = ViewModelProviders.of((FragmentActivity) activity).get(MyViewModel.class);

        model.initChild();

        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("deliveryman")
                .child(currentUserID+"/history");

        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

        dbReference.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildAdded | PREVIOUS CHILD" + s);
                if (dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("numberOfDishes") &&
                        dataSnapshot.hasChild("expectedTime") &&
                        dataSnapshot.hasChild("deliveredTime")) {

                    //retrieve history infos from DB
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                    String numDishes = dataSnapshot.child("numberOfDishes").getValue().toString();
                    String orderID = dataSnapshot.child("orderID").getValue().toString();
                    String priceStr = dataSnapshot.child("totalPrice").getValue()
                            .toString().replace(",", ".");
                    String restaurantAddress = dataSnapshot.child("addressRestaurant").getValue().toString();
                    String expectedTime = dataSnapshot.child("expectedTime").getValue().toString();
                    String deliveredTime = dataSnapshot.child("deliveredTime").getValue().toString();


                    HistoryItem historyObj = new HistoryItem(orderID, restaurantAddress,
                            nameRestaurant, priceStr, numDishes, expectedTime, deliveredTime);

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //History item cannot change
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //History item cannot be deleted
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //History item cannot be moved
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
