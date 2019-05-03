package com.mad.poleato.Rides;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RidesFragment extends Fragment {

    private Toast myToast;

    private Activity hostActivity;
    private View fragView;
    private RidesRecyclerViewAdapter ridesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
    private DatabaseReference dbReferece;

    private HashMap<String, Ride>rideMap;
    private List<Ride>rideList;
    private List<Ride> currDisplayedList; //list of filtered elements displayed on the screen

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(hostActivity!=null){
            myToast = Toast.makeText(hostActivity,"", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rideList = new ArrayList<>();
        rideMap = new HashMap<>();
        currDisplayedList = new ArrayList<>();

        if(getActivity() != null){
            //TODO: update strings.xml
            progressDialog = ProgressDialog.show(getActivity(), "", "Loading");
        }

        fillFields();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.ride_recyclerview, container,false);
        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = (RecyclerView) fragView.findViewById(R.id.rides_recyclerview);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.ridesAdapter = new RidesRecyclerViewAdapter(this.hostActivity,this.currDisplayedList);
        rv.setAdapter(ridesAdapter);
        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

    }

    public void fillFields(){
        dbReferece = FirebaseDatabase.getInstance().getReference("deliveryman").child("D00").child("reservations");

        dbReferece.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);
            }
        });

        dbReferece.addChildEventListener(new ChildEventListener() {
            List<Ride>childItem;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
                String orderID = dataSnapshot.getKey();
                String addressCustomer = dataSnapshot.child("addressCustomer").getValue().toString();
                String addressRestaurant = dataSnapshot.child("addressRestaurant").getValue().toString();
                String nameRestaurant = dataSnapshot.child("nameRestaurant").getValue().toString();
                String surnameCustomer = dataSnapshot.child("surnameCustomer").getValue().toString();
                Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").getValue().toString());
                Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").getValue().toString());

                Ride rideObj = new Ride(orderID,surnameCustomer,addressCustomer,nameRestaurant,addressRestaurant,totalPrice,numberOfDishes);

                rideMap.put(orderID,rideObj);
                rideList.add(rideObj);
                addToDisplay(rideObj);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("fabio", "onChildAdded | PREVIOUS CHILD" + s);
                String orderID = dataSnapshot.getKey();
                String addressCustomer = dataSnapshot.child("addressCustomer").toString();
                String addressRestaurant = dataSnapshot.child("addressRestaurant").toString();
                String nameRestaurant = dataSnapshot.child("nameRestaurant").toString();
                String surnameCustomer = dataSnapshot.child("surnameCustomer").toString();
                Double totalPrice = Double.parseDouble(dataSnapshot.child("totalPrice").toString());
                Integer numberOfDishes = Integer.parseInt(dataSnapshot.child("numberOfDishes").toString());

                Ride rideObj = new Ride(orderID,surnameCustomer,addressCustomer,nameRestaurant,addressRestaurant,totalPrice,numberOfDishes);

                rideMap.put(orderID,rideObj);
                rideList.add(rideObj);


                ridesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                Ride toRemove = rideMap.get(id);
                rideMap.remove(id);
                rideList.remove(toRemove);
                removeFromDisplay(toRemove);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
    }
    private void addToDisplay(Ride r) {
        //add new item to the displayed list
        currDisplayedList.add(r);
        //notify the adapter to show it by keeping the actual order
        ridesAdapter.notifyDataSetChanged();
    }

    private void removeFromDisplay(Ride r) {

        currDisplayedList.remove(r);
        // simply update. No order is compromised by removing an item
        ridesAdapter.notifyDataSetChanged();
    }

}
