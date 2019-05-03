package com.mad.poleato.View.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Rides.Ride;

import java.util.HashMap;
import java.util.List;


public class MyViewModel extends ViewModel {
    private MutableLiveData<HashMap<String, Ride>> _MapDataRides = new MutableLiveData<>(); // header titles

    public LiveData<HashMap<String, Ride>> getListR() {
        return _MapDataRides;
    }

    public void insertChild(String orderID, Ride ride) {
        this._MapDataRides.getValue().put(orderID, ride);
    }

    public void removeChild(final String orderID) {
        this._MapDataRides.getValue().remove(orderID);
    }

    public void testInitRiders () {
//        Ride ride = new Ride();
//        insertChild();
    }

//    public void fillFields() {
//
//        DatabaseReference dbReferece = FirebaseDatabase.getInstance().getReference("deliveryman").child("D00").child("reservations");
//
//        dbReferece.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //handler.sendEmptyMessage(0);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                //handler.sendEmptyMessage(0);
//            }
//        });
//
//        dbReferece.addChildEventListener(new ChildEventListener() {
//            List<Ride> childItem;
//
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
//                String orderID = dataSnapshot.getKey();
//                String addressCustomer = dataSnapshot.child("addressCustomer").getValue().toString();
//                String addressRestaurant = dataSnapshot.child("addressRestaurant").getValue().toString();
//                String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
//                String surnameCustomer = dataSnapshot.child("surnameCustomer").getValue().toString();
//                Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").getValue().toString());
//                Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").getValue().toString());
//
//                Ride rideObj = new Ride(orderID, surnameCustomer, addressCustomer, nameRestaurant, addressRestaurant, totalPrice, numberOfDishes);
//
//                //rideMap.put(orderID, rideObj);
//                insertChild(orderID, rideObj);
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
//                String orderID = dataSnapshot.getKey();
//                String addressCustomer = dataSnapshot.child("addressCustomer").toString();
//                String addressRestaurant = dataSnapshot.child("addressRestaurant").toString();
//                String nameRestaurant = dataSnapshot.child("nameRestaurant").toString();
//                String surnameCustomer = dataSnapshot.child("surnameCustomer").toString();
//                Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").toString());
//                Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").toString());
//
//                Ride rideObj = new Ride(orderID, surnameCustomer, addressCustomer, nameRestaurant, addressRestaurant, totalPrice, numberOfDishes);
//
//                //rideMap.put(orderID, rideObj);
//                insertChild(orderID, rideObj);
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                String id = dataSnapshot.getKey();
//
//                removeChild(id);
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//
//        });
//    }
}
