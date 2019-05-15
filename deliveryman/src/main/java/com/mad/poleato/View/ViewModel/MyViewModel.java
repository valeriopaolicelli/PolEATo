package com.mad.poleato.View.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.mad.poleato.FirebaseData.MyFirebaseData;
import com.mad.poleato.History.HistoryItem;

import java.util.HashMap;


public class MyViewModel extends ViewModel {
    private MutableLiveData<HashMap<String, HistoryItem>> _MapDataHistory;
    private MutableLiveData<Boolean> _showProgressBar;
    private MyFirebaseData myFirebaseData;

    public MyViewModel(){
        this.myFirebaseData = new MyFirebaseData();
        this._MapDataHistory = myFirebaseData.getMapDataHistory();
        if(myFirebaseData != null && _MapDataHistory != null){
            myFirebaseData.fillFieldsHistory();
        }
        this._showProgressBar = myFirebaseData.getShowProgressBar();
    }


    public LiveData<HashMap<String, HistoryItem>> getListH() {
//        if (_MapDataHistory == null)
//        _MapDataHistory = new MutableLiveData<>(); // header titles
        return _MapDataHistory;
    }

    public void insertChild(String orderID, HistoryItem history) {
        this._MapDataHistory.getValue().put(orderID, history);
        this._MapDataHistory.postValue(_MapDataHistory.getValue());
    }

    public void removeChild(final String orderID) {
        this._MapDataHistory.getValue().remove(orderID);
    }

    public void initChild(){
        _MapDataHistory.setValue(new HashMap<String, HistoryItem>());
    }

    public void testInitRiders () {
//        Ride ride = new Ride();
//        insertChild();
    }

//    public void fillFields() {
//
//        DatabaseReference dbReferece = FirebaseDatabase.getInstance()
//                              .getReference("deliveryman")
//                               .child(currentUserID+"/reservations");
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
