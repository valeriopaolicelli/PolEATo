package com.mad.poleato;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RestaurantSearchFragment extends DialogFragment {

    private Activity hostActivity;
    private View fragView;
    private RestaurantListAdapter listAdapter;
    private DatabaseReference dbReference;

    private HashMap<String, Restaurant> restaurantMap;
    private List<Restaurant> restaurantList;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        restaurantMap = new HashMap<>();
        restaurantList = new ArrayList<>();

        dbReference = FirebaseDatabase.getInstance().getReference("restaurants");
        dbReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.image_empty); // TODO: make it dynamic
                String name = dataSnapshot.child("Name").getValue().toString();
                String type = dataSnapshot.child("Type").getValue().toString();
                Boolean isOpen = (Boolean)dataSnapshot.child("IsActive").getValue();
                Restaurant resObj = new Restaurant(img, name, type, isOpen);

                String id = dataSnapshot.getKey();
                restaurantMap.put(id, resObj);
                restaurantList.add(resObj);


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurant_search_fragment, container, false);

        return fragView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.listAdapter = new RestaurantListAdapter(this.hostActivity, 0, this.restaurantList);

        ListView listV = (ListView)fragView.findViewById(R.id.restaurantList);
        listV.setAdapter(listAdapter);

    }

    private void initRestaurantList(){



    }
}
