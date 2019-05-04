package com.mad.poleato.FirebaseData;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Rides.Ride;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.util.List;
import java.util.logging.Handler;

public class MyFirebaseData {

    private MyViewModel model;
    private Activity activity;
    private Handler handler;
    private ProgressDialog progressDialog;

    public MyFirebaseData(Activity activity, ProgressDialog progressDialog) {
        this.activity = activity;
        this.progressDialog = progressDialog;
    }

    public void fillFieldsRiders() {

        model = ViewModelProviders.of((FragmentActivity) activity).get(MyViewModel.class);

        DatabaseReference dbReferece = FirebaseDatabase.getInstance().getReference("deliveryman").child("D00").child("reservations");

        dbReferece.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //handler.sendEmptyMessage(0);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //handler.sendEmptyMessage(0);
                progressDialog.dismiss();
            }
        });

        dbReferece.addChildEventListener(new ChildEventListener() {
            List<Ride> childItem;

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
                if(dataSnapshot.hasChild("orderID") &&
                    dataSnapshot.hasChild("addressCustomer") &&
                    dataSnapshot.hasChild("surnameCustomer") &&
                    dataSnapshot.hasChild("nameRestaurant") &&
                    dataSnapshot.hasChild("addressRestaurant") &&
                    dataSnapshot.hasChild("totalPrice") &&
                    dataSnapshot.hasChild("numberOfDishes")) {

                    String orderID = dataSnapshot.child("orderID").getValue().toString();
                    String addressCustomer = dataSnapshot.child("addressCustomer").getValue().toString();
                    String addressRestaurant = dataSnapshot.child("addressRestaurant").getValue().toString();
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                    String surnameCustomer = dataSnapshot.child("surnameCustomer").getValue().toString();
                    Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").getValue().toString());
                    Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").getValue().toString());

                    Ride rideObj = new Ride(orderID, surnameCustomer, addressCustomer, nameRestaurant, addressRestaurant, totalPrice, numberOfDishes);

                    model.insertChild(orderID, rideObj);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
                if(dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("addressCustomer") &&
                        dataSnapshot.hasChild("surnameCustomer") &&
                        dataSnapshot.hasChild("nameRestaurant") &&
                        dataSnapshot.hasChild("addressRestaurant") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("numberOfDishes")) {

                    String orderID = dataSnapshot.getKey();
                    String addressCustomer = dataSnapshot.child("addressCustomer").toString();
                    String addressRestaurant = dataSnapshot.child("addressRestaurant").toString();
                    String nameRestaurant = dataSnapshot.child("nameRestaurant").toString();
                    String surnameCustomer = dataSnapshot.child("surnameCustomer").toString();
                    Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").toString());
                    Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").toString());

                    Ride rideObj = new Ride(orderID, surnameCustomer, addressCustomer, nameRestaurant, addressRestaurant, totalPrice, numberOfDishes);

                    model.insertChild(orderID, rideObj);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();

                model.removeChild(id);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
}
