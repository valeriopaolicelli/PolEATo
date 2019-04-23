package com.mad.poleato;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class RestaurantSearchFragment extends DialogFragment {

    private Activity hostActivity;
    private View fragView;
    private RestaurantRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
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
                Log.d("matte", "onChildAdded | PREVIOUS CHILD: "+s);

                String id = dataSnapshot.getKey();
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.image_empty); // TODO: make it dynamic
                String name = dataSnapshot.child("Name").getValue().toString();
                String type = dataSnapshot.child("Type").getValue().toString();
                Boolean isOpen = (Boolean)dataSnapshot.child("IsActive").getValue();
                Restaurant resObj = new Restaurant(id, img, name, type, isOpen);

                restaurantMap.put(id, resObj);
                restaurantList.add(resObj);

                recyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildChanged | PREVIOUS CHILD: "+s);


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("matte", "onChildRemoved");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildMoved | PREVIOUS CHILD: "+s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: "+databaseError.getDetails()+
                            " | MESSAGE: "+databaseError.getMessage());
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurant_recyclerview, container, false);

        return fragView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SearchView sv = (SearchView) fragView.findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            List<Restaurant> currList;
            int previousLen, currLen;
            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d("matte", "queryTextSubmit | QUERY: "+query);


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("matte", "queryTextChange | QUERY: "+newText);

                currLen = newText.length();

                if(currList == null){
                    previousLen = -1;
                    //first time
                    currLen = 0;
                    currList = new ArrayList<>();
                    //initialize the currList
                    for(Restaurant r : restaurantList)
                        currList.add(r);
                }

                if(newText.isEmpty()){
                    //void query
                    //reset the currList
                    currList.clear();
                    for(Restaurant r : restaurantList)
                        currList.add(r);
                    //reset the adapter list to show all the original items
                    recyclerAdapter.setResList(restaurantList);
                    previousLen = currLen;
                    return true;
                }


                if(currLen > previousLen)
                {
                    //text added to query: search from previous list
                    Iterator<Restaurant> rIterator = currList.iterator();
                    while (rIterator.hasNext()) {
                        Restaurant s = rIterator.next(); // must be called before you can call i.remove()
                        // Do something
                        if(!s.getName().toLowerCase().contains(newText.toLowerCase()))
                            rIterator.remove();
                    }
                }
                else{
                    //text removed from query: search from the whole list
                    currList.clear();
                    for(Restaurant r : restaurantList)
                        currList.add(r);
                    Iterator<Restaurant> rIterator = currList.iterator();
                    while (rIterator.hasNext()) {
                        Restaurant s = rIterator.next(); // must be called before you can call i.remove()
                        // Do something
                        if(!s.getName().toLowerCase().contains(newText.toLowerCase()))
                            rIterator.remove();
                    }
                }


                recyclerAdapter.setResList(currList);

                Log.d("matte", "queryTextChange | currList size: "+currList.size());
                previousLen = currLen;
                return true;

            }
        });



        rv = (RecyclerView)fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new RestaurantRecyclerViewAdapter(this.hostActivity, this.restaurantList);
        rv.setAdapter(recyclerAdapter);

        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

    }

}
