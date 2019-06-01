package com.mad.poleato.AboutUs.ReviewManagement;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantReviewsFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;

    private ReviewRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private RatingBar ratingBar;
    private TextView noRating;
    private CheckBox onlyCommentCheckBox;
    private String resName;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private BottomNavigationView navigation;

    private long totalReviews;
    private float avgReviews;
    private int totalStars;

    HashMap<String, MyDatabaseReference> dbReferenceList;

    private HashMap<String, Rating>reviewsMap;
    private List<Rating>reviewsList;
    private List<Rating>displayedList;

    private ProgressDialog progressDialog;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = hostActivity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        reviewsMap = new HashMap<>();
        reviewsList = new ArrayList<>();
        displayedList = new ArrayList<>();

        dbReferenceList= new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        DatabaseReference reviewsReference = FirebaseDatabase.getInstance().getReference("restaurants/"+ currentUserID + "/Ratings");
        dbReferenceList.put("ratings", new MyDatabaseReference(reviewsReference));

        if(getActivity() != null){
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        }
        fillFields();
    }

    public RestaurantReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurantreviews_fragment_layout, container, false);
        // Inflate the layout for this fragment

        navigation = getActivity().findViewById(R.id.navigation);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ratingBar = (RatingBar) fragView.findViewById(R.id.rating_bar_avg);
        onlyCommentCheckBox = (CheckBox) fragView.findViewById(R.id.checkBoxComments);
        noRating = (TextView) fragView.findViewById(R.id.noRatingsTv);

        rv = (RecyclerView) fragView.findViewById(R.id.reviews_rv);
        rv.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerViewAdapter = new ReviewRecyclerViewAdapter(this.hostActivity,displayedList);
        rv.setAdapter(recyclerViewAdapter);

        onlyCommentCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    restoreOriginalList();
                }else
                {
                    filterList();
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        /** Hide bottomBar for this fragment*/
        navigation.setVisibility(View.GONE);
    }

    public void fillFields(){

        dbReferenceList.get("ratings").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);

            }
        });


        dbReferenceList.get("ratings").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                if (ds.exists()) {
                    final String[] customerName = new String[1];
                    final String[] customerSurname = new String[1];

                    if (ds.hasChild("comment") &&
                            ds.hasChild("customerID") &&
                            ds.hasChild("orderID") &&
                            ds.hasChild("rate") &&
                            ds.hasChild("restaurantID")) {
                        String customerID = ds.child("customerID").getValue().toString();
                        int rate = Integer.parseInt(ds.child("rate").getValue().toString());
                        final String date = ds.child("date").getValue().toString();
                        String comment = ds.child("comment").getValue().toString();

                        totalStars+=rate;
                        totalReviews++;
                        avgReviews = (float)totalStars/totalReviews;
                        ratingBar.setRating(avgReviews);

                        final Rating rating = new Rating(customerID, rate, comment, currentUserID, ds.getKey(),date);
                        reviewsList.add(rating);
                        displayedList.add(rating);
                        reviewsMap.put(ds.getKey(), rating);
//Get customer data
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customers/" + customerID);
                        dbReferenceList.put("customerAdded", new MyDatabaseReference(customerReference));

                        dbReferenceList.get("customerAdded").setValueListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                customerName[0] = dataSnapshot.child("Name").getValue().toString();
                                customerSurname[0] = dataSnapshot.child("Surname").getValue().toString();
                                rating.setCustomerData(customerName[0] + " " + customerSurname[0]);
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        Collections.sort(displayedList,Rating.timeComparator);
                        recyclerViewAdapter.notifyDataSetChanged();

                    }
                }else{
                    //no ratings
                    ratingBar.setRating(0);
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) {
                if (ds.exists()) {
                    final String[] customerName = new String[1];
                    final String[] customerSurname = new String[1];

                    if (ds.hasChild("comment") &&
                            ds.hasChild("customerID") &&
                            ds.hasChild("orderID") &&
                            ds.hasChild("rate") &&
                            ds.hasChild("restaurantID")) {
                        String customerID = ds.child("customerID").getValue().toString();
                        int rate = Integer.parseInt(ds.child("rate").getValue().toString());
                        final String date = ds.child("date").getValue().toString();
                        String comment = ds.child("comment").getValue().toString();

                        totalStars += rate;
                        totalReviews++;
                        avgReviews = (float) totalStars / totalReviews;
                        ratingBar.setRating(avgReviews);

                        final Rating rating = new Rating(customerID, rate, comment, currentUserID, ds.getKey(), date);
                        if(!reviewsMap.containsKey(ds.getKey())){
                            displayedList.add(rating);
                        }else{
                            for(Rating r : reviewsList){
                                if(r.getOrderID().equals(rating.getOrderID())){
                                    r = rating;
                                }
                            }
                        }
                        reviewsMap.put(ds.getKey(), rating);
//Get customer data
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customers/" + customerID);
                        dbReferenceList.put("customerChanged", new MyDatabaseReference(customerReference));

                        dbReferenceList.get("customerChanged").setValueListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                customerName[0] = dataSnapshot.child("Name").getValue().toString();
                                customerSurname[0] = dataSnapshot.child("Surname").getValue().toString();
                                rating.setCustomerData(customerName[0] + " " + customerSurname[0]);
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        Collections.sort(displayedList,Rating.timeComparator);
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    String id = dataSnapshot.getKey();
                    Rating toRemove =  reviewsMap.get(id);
                    reviewsMap.remove(id);
                    removeFromDisplayable(toRemove);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void restoreOriginalList(){
        displayedList.clear();
        displayedList.addAll(reviewsList);
    }

    private void removeFromDisplayable(Rating rating){
        displayedList.remove(rating);
        recyclerViewAdapter.notifyDataSetChanged();
    }


    private void filterList(){
        if(displayedList.isEmpty())
            return;

        Iterator<Rating>iterator= displayedList.iterator();
        while (iterator.hasNext()){
            Rating r = iterator.next();
            if(!isValidToDsplay(r)){
                iterator.remove();
            }
        }
    }

    private boolean isValidToDsplay(Rating rating){
        return !rating.getComment().equals("");
    }

    @Override
    public void onStop() {
        super.onStop();

        navigation.setVisibility(View.VISIBLE);

        for(MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for(MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}
