package com.mad.poleato;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.Condition;


public class RestaurantSearchFragment extends DialogFragment {

    private Activity hostActivity;
    private View fragView;
    private RestaurantRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
    private SearchView sv;
    private ImageButton sortBtn;
    private ImageButton filterBtn;
    private DatabaseReference dbReference;

    private HashMap<String, Restaurant> restaurantMap;
    private List<Restaurant> restaurantList; //original list of all restaurants
    private List<Restaurant> currDisplayedList; //list of filtered elements displayed on the screen
    private Set<String> typesToFilter;

    ProgressDialog progressDialog;
    private long totalItem;
    Toast myToast;

    Condition e;


    //id for the filter fragment
    public static final int FILTER_FRAGMENT = 26;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(hostActivity != null)
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        totalItem = 0;

        restaurantMap = new HashMap<>();
        restaurantList = new ArrayList<>();
        typesToFilter = new HashSet<>();
        currDisplayedList = new ArrayList<>();

    }

    @Override
    public void onResume() {
        super.onResume();
       // if(getActivity() != null)
          //  progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));



        //Producer & consumer pattern


        downloadInfos();
        //wait for signaling
        //downloadImages();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurant_recyclerview, container, false);

        return fragView;

    }


    private void downloadInfos() {
        Locale locale = Locale.getDefault();
        // get "en" or "it"
        final String localeShort = locale.toString().substring(0, 2);

        dbReference = FirebaseDatabase.getInstance().getReference("restaurants");
       /* dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                totalItem = dataSnapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
        dbReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildAdded | PREVIOUS CHILD: " + s);

                String id = dataSnapshot.getKey();
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.image_empty); // TODO: make it dynamic
                String name = dataSnapshot.child("Name").getValue().toString();
                String type = dataSnapshot.child("Type").child(localeShort).getValue().toString();
                Boolean isOpen = (Boolean) dataSnapshot.child("IsActive").getValue();
                int priceRange = Integer.parseInt(dataSnapshot.child("PriceRange").getValue().toString());
                double deliveryCost = Double.parseDouble(dataSnapshot.child("DeliveryCost").getValue().toString().replace(",", "."));

                Restaurant resObj = new Restaurant(id, img, name, type, isOpen, priceRange, deliveryCost);
                //add to the original list
                restaurantMap.put(id, resObj);
                restaurantList.add(resObj);
                //check the filter before display
                if (isValidToDisplay(resObj))
                    addToDisplay(resObj);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildChanged | PREVIOUS CHILD: " + s);

                String id = dataSnapshot.getKey();
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.image_empty); // TODO: make it dynamic
                String name = dataSnapshot.child("Name").getValue().toString();
                String type = dataSnapshot.child("Type").child(localeShort).getValue().toString();
                Boolean isOpen = (Boolean) dataSnapshot.child("IsActive").getValue();
                int priceRange = Integer.parseInt(dataSnapshot.child("PriceRange").getValue().toString());
                double deliveryCost = Double.parseDouble(dataSnapshot.child("DeliveryCost").getValue().toString().replace(",", "."));

                Restaurant resObj = restaurantMap.get(id);
                resObj.setImage(img);
                resObj.setName(name);
                resObj.setType(type);
                resObj.setIsOpen(isOpen);
                resObj.setPriceRange(priceRange);
                resObj.setDeliveryCost(deliveryCost);
                //insert the element by keeping the actual order after checking the filter
                if (isValidToDisplay(resObj))
                    recyclerAdapter.updateLayout();

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("matte", "onChildRemoved");

                String id = dataSnapshot.getKey();
                Restaurant toRemove = restaurantMap.get(id);
                restaurantMap.remove(id);
                restaurantList.remove(toRemove);
                removeFromDisplay(toRemove);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildMoved | PREVIOUS CHILD: " + s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
            }
        });

    }

    public void downloadImages(){


        //Download the profile pic
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child("/ProfileImage/img.jpg");

      /*  final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
                //send message to main thread
                handler.sendEmptyMessage(0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(getActivity() != null){
                    myToast.setText("No Such file or Path found!!");
                    myToast.show();
                }
                else
                    Log.d("matte", "null context and profilePic download failed");
                //set predefined image
                profileImage.setImageResource(R.drawable.plate_fork);
                //send message to main thread
                handler.sendEmptyMessage(0);
            }
        });*/

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        sv = (SearchView) fragView.findViewById(R.id.searchView);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            int previousLen, currLen;

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("matte", "queryTextSubmit | QUERY: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("matte", "queryTextChange | QUERY: " + newText);

                if (newText == null)
                    return false;
                currLen = newText.length();

                //void query: reset the list
                if (currLen == 0) {
                    restoreOriginalList();
                    filterDisplay();
                } else {
                    //if text removed from query: search from the whole list
                    if (currLen < previousLen) {
                        restoreOriginalList();
                        filterDisplay();
                    }
                    //text added to query: search from previous list
                    Iterator<Restaurant> rIterator = currDisplayedList.iterator();
                    while (rIterator.hasNext()) {
                        Restaurant s = rIterator.next(); // must be called before you can call i.remove()
                        if (!s.getName().toLowerCase().contains(newText.toLowerCase()))
                            rIterator.remove();
                    }
                }
                //display the updated list
                recyclerAdapter.display(currDisplayedList);

                Log.d("matte", "queryTextChange | displayed list size: " + currDisplayedList.size());
                previousLen = currLen;
                return true;
            }
        });


        rv = (RecyclerView) fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new RestaurantRecyclerViewAdapter(this.hostActivity, this.currDisplayedList);
        rv.setAdapter(recyclerAdapter);

        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        sortBtn = (ImageButton) fragView.findViewById(R.id.sortButton);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(hostActivity, sortBtn);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String id = fragView.getResources().getResourceName(item.getItemId());
                        Log.d("matte", "Popup pressed | ID: " + id);

                        String[] itemName = id.split("/");
                        if (itemName.length > 2) //error
                            return false;
                        Log.d("matte", "NAME: " + itemName[1]);

                        if (itemName[1].equals("sortByName")) {
                            Log.d("matte", "SORT: byName");
                            recyclerAdapter.sortByName();
                            return true;
                        }

                        if (itemName[1].equals("sortByPrice")) {
                            Log.d("matte", "SORT: byPrice");
                            recyclerAdapter.sortByPrice();
                            return true;
                        }

                        if (itemName[1].equals("sortByPriceInverse")) {
                            Log.d("matte", "SORT: byPriceInverse");
                            recyclerAdapter.sortByPriceInverse();
                            return true;
                        }

                        if (itemName[1].equals("sortByDelivery")) {
                            Log.d("matte", "SORT: byDelivery");
                            recyclerAdapter.sortByDelivery();
                            return true;
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });


        filterBtn = (ImageButton) fragView.findViewById(R.id.filterButton);
        final Fragment f = this;
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("filter_fragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                FilterFragment filterFrag = new FilterFragment();
                Bundle bundle = new Bundle();
                //pass the current checkbox state to the fragment
                bundle.putSerializable("checkbox_state", (HashSet<String>)typesToFilter);
                filterFrag.setArguments(bundle);
                filterFrag.setTargetFragment(f, FILTER_FRAGMENT);
                filterFrag.show(ft, "filter_fragment");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILTER_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    typesToFilter = (HashSet<String>) bundle.getSerializable("checked_types");

                    restoreOriginalList();
                    if(!typesToFilter.isEmpty())
                        filterDisplay();

                    recyclerAdapter.display(currDisplayedList);
                }
                break;
        }

    }


    private void restoreOriginalList() {
        currDisplayedList.clear();
        for (Restaurant r : restaurantList)
            currDisplayedList.add(r);
    }


    private void addToDisplay(Restaurant r) {
        //add new item to the displayed list
        currDisplayedList.add(r);
        //notify the adapter to show it by keeping the actual order
        recyclerAdapter.updateLayout();
    }

    private void removeFromDisplay(Restaurant r) {

        currDisplayedList.remove(r);
        // simply update. No order is compromised by removing an item
        recyclerAdapter.notifyDataSetChanged();
    }

    private void filterDisplay() {
        //no filters to apply
        if (typesToFilter.isEmpty())
            return;

        Iterator<Restaurant> rIterator = currDisplayedList.iterator();
        while (rIterator.hasNext()) {
            Restaurant s = rIterator.next();
            if (!isValidToDisplay(s))
                rIterator.remove();
        }


    }

    private boolean isValidToDisplay(Restaurant r) {
        //no filters to apply -> always valid
        if (typesToFilter.isEmpty())
            return true;

        String[] types = r.getType().toLowerCase().split(",(\\s)*");
        for (String t : types) {
            if (typesToFilter.contains(t)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRemovedFilter(Set<String> newFilters){
        for(String s : typesToFilter){
            //if a filter was removed
            if(!newFilters.contains(s))
                return true;
        }

        return false;
    }


}
