package com.mad.poleato.FavoriteRestaurants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.Classes.Restaurant;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment that handle favorite restaurants of the customers
 */
public class FavoriteRestaurantFragment extends Fragment {

    private Toast myToast;
    private Activity hostActivity;
    private View fragView;
    private FavoriteRestaurantRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private DatabaseReference dbReference;

    private HashMap<String, Restaurant> restaurantMap;
    private List<Restaurant> restaurantList; //original list of all restaurants
    private List<Restaurant> currDisplayedList; //list of filtered elements displayed on the screen
    private Set<String> typesToFilter;

    private ProgressDialog progressDialog;

    private ImageView empty_view;


    //id for the filter fragment
    public static final int FILTER_FRAGMENT = 26;

    // list to collect the firebase reference and its listener (to remove listener at the end of this fragment)
    private HashMap<String, MyDatabaseReference> dbReferenceList;

    // attributes for retrieve the current user logged
    private String currentUserID;
    private FirebaseAuth mAuth;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(hostActivity != null)
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        restaurantMap = new HashMap<>();
        restaurantList = new ArrayList<>();
        typesToFilter = new HashSet<>();
        currDisplayedList = new ArrayList<>();

        dbReferenceList= new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        fillFields();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.favorite_restaurant_recyclerview, container, false);

        return fragView;
    }

    private void show_empty_view(){

        rv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    private void show_main_view(){

        empty_view.setVisibility(View.GONE);
        rv.setVisibility(View.VISIBLE);
    }


    private void fillFields() {
        Locale locale = Locale.getDefault();
        // get "en" or "it"
        final String localeShort = locale.toString().substring(0, 2);

        dbReference = FirebaseDatabase.getInstance().getReference("customers").child(currentUserID+"/Favorite");

        /**
         *         This listener is guaranteed to be called only after "ChildEvent".
         *         Thus it notifies the end of the children
         */
        dbReferenceList.put("favorite", new MyDatabaseReference(dbReference));
        final int indexReference= dbReferenceList.size()-1;

        dbReferenceList.get("favorite").setValueListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "ValueEventiListener : OnCancelled() invoked");
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

        dbReferenceList.get("favorite").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshotRestaurant, @Nullable String s) {
                final String restaurantID = dataSnapshotRestaurant.getKey();
                Log.d("ValerioListener", restaurantID);

                DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants");
                dbReferenceList.put("restaurant", new MyDatabaseReference(referenceRestaurant));

                dbReferenceList.get("restaurant").setValueListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("matte", "ValueEventiListener : OnCancelled() invoked");
                        if(progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

                dbReferenceList.get("restaurant").setChildListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshotRestaurant, @Nullable String s) {
                        Log.d("matte", "onChildAdded | PREVIOUS CHILD: " + s);
                        if(dataSnapshotRestaurant.getKey().equals(restaurantID)) {
                            if (dataSnapshotRestaurant.hasChild("Name") &&
                                    dataSnapshotRestaurant.hasChild("Type") &&
                                    dataSnapshotRestaurant.child("Type").hasChild("it") &&
                                    dataSnapshotRestaurant.child("Type").hasChild("en") &&
                                    dataSnapshotRestaurant.hasChild("IsActive") &&
                                    dataSnapshotRestaurant.hasChild("PriceRange") &&
                                    dataSnapshotRestaurant.hasChild("DeliveryCost") &&
                                    dataSnapshotRestaurant.hasChild("photoUrl")
                            ) {
                                final String id = dataSnapshotRestaurant.getKey();
                                String name = dataSnapshotRestaurant.child("Name").getValue().toString();
                                String type = dataSnapshotRestaurant.child("Type").child(localeShort).getValue().toString();
                                Boolean isOpen = (Boolean) dataSnapshotRestaurant.child("IsActive").getValue();
                                int priceRange = Integer.parseInt(dataSnapshotRestaurant.child("PriceRange").getValue().toString());
                                double deliveryCost = Double.parseDouble(dataSnapshotRestaurant.child("DeliveryCost").getValue().toString().replace(",", "."));
                                final String imageUrl = dataSnapshotRestaurant.child("photoUrl").getValue().toString();
                                //insert before the download because otherwise it can happen that the download finish before
                                //  and the put raise exception
                                Restaurant resObj = new Restaurant(id, "", name, type, isOpen, priceRange, deliveryCost);
                                //Check if restaurant has reviews
                                if(!dataSnapshotRestaurant.hasChild("Ratings")){
                                    resObj.setTotalReviews((long)0);
                                    resObj.computeAvgStars(0);
                                }else{
                                    //If yes, compute avg rating
                                    int totalStars = 0;
                                    resObj.setTotalReviews(dataSnapshotRestaurant.child("Ratings").getChildrenCount());
                                    for (DataSnapshot ds : dataSnapshotRestaurant.child("Ratings").getChildren()){
                                        totalStars += Integer.parseInt(ds.child("rate").getValue().toString());
                                    }
                                    resObj.computeAvgStars(totalStars);
                                }
                                restaurantMap.put(id, resObj);
                                restaurantList.add(resObj);

                                StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                final long ONE_MEGABYTE = 1024 * 1024;
                                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        String s = imageUrl;
                                        Log.d("matte", "onSuccess | restaurantID: " + id);
                                        // Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        restaurantMap.get(id).setImage(s);
                                        recyclerAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        String s = "";
                                        Log.d("matte", "onFailure() : excp -> " + exception.getMessage()
                                                + "| restaurantID: " + id);
                                    }
                                });


                                //check the filter before display
                                if (isValidToDisplay(resObj))
                                    addToDisplay(resObj);
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshotRestaurant, @Nullable String s) {
                        Log.d("matte", "onChildChanged | PREVIOUS CHILD: " + s);
                        if(dataSnapshotRestaurant.getKey().equals(restaurantID)) {
                            if (dataSnapshotRestaurant.hasChild("Name") &&
                                    dataSnapshotRestaurant.hasChild("Type") &&
                                    dataSnapshotRestaurant.child("Type").hasChild("it") &&
                                    dataSnapshotRestaurant.child("Type").hasChild("en") &&
                                    dataSnapshotRestaurant.hasChild("IsActive") &&
                                    dataSnapshotRestaurant.hasChild("PriceRange") &&
                                    dataSnapshotRestaurant.hasChild("DeliveryCost") &&
                                    dataSnapshotRestaurant.hasChild("photoUrl")
                            ) {
                                final String id = dataSnapshotRestaurant.getKey();
                                String name = dataSnapshotRestaurant.child("Name").getValue().toString();
                                String type = dataSnapshotRestaurant.child("Type").child(localeShort).getValue().toString();
                                Boolean isOpen = (Boolean) dataSnapshotRestaurant.child("IsActive").getValue();
                                int priceRange = Integer.parseInt(dataSnapshotRestaurant.child("PriceRange").getValue().toString());
                                double deliveryCost = Double.parseDouble(dataSnapshotRestaurant.child("DeliveryCost").getValue().toString().replace(",", "."));
                                final String imageUrl = dataSnapshotRestaurant.child("photoUrl").getValue().toString();

                                Restaurant resObj;
                                if (restaurantMap.containsKey(id)) {
                                    resObj = restaurantMap.get(id);
                                    resObj.setName(name);
                                    resObj.setType(type);
                                    resObj.setIsOpen(isOpen);
                                    resObj.setPriceRange(priceRange);
                                    resObj.setDeliveryCost(deliveryCost);
                                    //Check if restaurant has reviews
                                    if(!dataSnapshotRestaurant.hasChild("Ratings")){
                                        resObj.setTotalReviews((long)0);
                                        resObj.computeAvgStars(0);
                                    }else{
                                        //If yes, compute avg rating
                                        int totalStars = 0;
                                        resObj.setTotalReviews(dataSnapshotRestaurant.child("Ratings").getChildrenCount());
                                        for (DataSnapshot ds : dataSnapshotRestaurant.child("Ratings").getChildren()){
                                            totalStars += Integer.parseInt(ds.child("rate").getValue().toString());
                                        }
                                        resObj.computeAvgStars(totalStars);
                                    }
                                    //insert the element by keeping the actual order after checking the filter
                                    if (isValidToDisplay(resObj)) {
                                        if (!currDisplayedList.contains(resObj))
                                            addToDisplay(resObj);
                                    }
                                    else if (currDisplayedList.contains(resObj))
                                        removeFromDisplay(resObj);
                                } else {
                                    resObj = new Restaurant(id, "", name, type, isOpen, priceRange, deliveryCost);
                                    restaurantMap.put(id, resObj);
                                    restaurantList.add(resObj);
                                    if (isValidToDisplay(resObj))
                                        addToDisplay(resObj);
                                }

                                //check the new image
                                StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                final long ONE_MEGABYTE = 1024 * 1024;
                                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Log.d("matte", "onSuccess");
                                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        restaurantMap.get(id).setImage(imageUrl);
                                        recyclerAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.d("matte", "onFailure() : excp -> " + exception.getMessage());
                                        restaurantMap.get(id).setImage("");
                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshotRestaurant) {
                        Log.d("matte", "onChildRemoved");
                        String id = dataSnapshotRestaurant.getKey();
                        if(id.equals(restaurantID)) {
                            Restaurant toRemove = restaurantMap.get(id);
                            restaurantMap.remove(id);
                            restaurantList.remove(toRemove);
                            removeFromDisplay(toRemove);
                        }
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

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // nothing to do
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String restaurantID = dataSnapshot.getKey();
                Log.d("ValerioListener", restaurantID);
                Restaurant toRemove = restaurantMap.get(restaurantID);
                restaurantMap.remove(restaurantID);
                restaurantList.remove(toRemove);
                removeFromDisplay(toRemove);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // nothing to do
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = (RecyclerView) fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new FavoriteRestaurantRecyclerViewAdapter(this.hostActivity, this.currDisplayedList);
        rv.setAdapter(recyclerAdapter);

        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        empty_view = (ImageView) fragView.findViewById(R.id.favorite_empty_view);

        show_empty_view();
    }

    private void addToDisplay(Restaurant r) {
        //add new item to the displayed list
        currDisplayedList.add(r);
        show_main_view();
    }

    private void removeFromDisplay(Restaurant r) {

        currDisplayedList.remove(r);
        // simply update. No order is compromised by removing an item
        recyclerAdapter.notifyDataSetChanged();
        if(currDisplayedList.isEmpty())
            show_empty_view();
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

    @Override
    public void onStop() {
        super.onStop();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }
}
