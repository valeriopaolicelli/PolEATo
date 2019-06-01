package com.mad.poleato.MyReviews;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
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
import com.mad.poleato.AuthenticatorC.Authenticator;
import com.mad.poleato.Classes.Rating;
import com.mad.poleato.Classes.Restaurant;
import com.mad.poleato.FavoriteRestaurants.FavoriteRestaurantRecyclerViewAdapter;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/**
 * This fragment shows all the customer reviews and the overall of his reviews
 */
public class MyReviewsFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;

    private MyReviewsRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private RatingBar ratingBar;
    private CheckBox onlyCommentCheckBox;

    private String currentUserID;
    private FirebaseAuth mAuth;

    private long totalReviews;
    private float avgReviews;
    private int totalStars;

    private MyDatabaseReference reviewsReference;

    private HashMap<String, Rating>reviewsMap;
    private List<Rating>reviewsList;
    private List<Rating>displayedList;

    private ProgressDialog progressDialog;

    //views for this layout
    private CardView main_cardview;
    private ImageView empty_view;

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


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        reviewsReference = new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("customers/"+ currentUserID + "/Ratings"));

        if(getActivity() != null){
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        }
        fillFields();
    }

    public MyReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_my_reviews, container, false);
        // Inflate the layout for this fragment

        /** Logout a priori if access is revoked */
        if(currentUserID == null)
            Authenticator.revokeAccess(Objects.requireNonNull(getActivity()), fragView);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ratingBar = (RatingBar) fragView.findViewById(R.id.rating_bar_avg);
        onlyCommentCheckBox = (CheckBox) fragView.findViewById(R.id.checkBoxComments);

        rv = (RecyclerView) fragView.findViewById(R.id.reviews_rv);
        rv.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerViewAdapter = new MyReviewsRecyclerViewAdapter(this.hostActivity,displayedList);
        rv.setAdapter(recyclerViewAdapter);

        //checkbox to filter reviews with non-empty comments
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

        empty_view = (ImageView) fragView.findViewById(R.id.reviews_empty_view);
        main_cardview = (CardView) fragView.findViewById(R.id.reviews_cardview);

        show_empty_view();
    }


    private void show_empty_view(){

        main_cardview.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    private void show_main_view(){

        empty_view.setVisibility(View.GONE);
        main_cardview.setVisibility(View.VISIBLE);
        rv.setVisibility(View.VISIBLE);
    }


    /**
     * Method dedicate to fill the view elements
     * Getting data from database
     */
    public void fillFields(){

        reviewsReference.setValueListener(new ValueEventListener() {
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

        //getting all ratings of the customer
        reviewsReference.setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                if (ds.exists()) {

                    if (ds.hasChild("comment") &&
                            ds.hasChild("customerID") &&
                            ds.hasChild("orderID") &&
                            ds.hasChild("rate") &&
                            ds.hasChild("restaurantID")) {
                        String customerID = ds.child("customerID").getValue().toString();
                        String restaurantID = ds.child("restaurantID").getValue().toString();
                        int rate = Integer.parseInt(ds.child("rate").getValue().toString());
                        final String date = ds.child("date").getValue().toString();
                        String comment = ds.child("comment").getValue().toString();

                        //Compute overall rating of restaurant selected
                        totalStars+=rate;
                        totalReviews++;
                        avgReviews = (float)totalStars/totalReviews;
                        ratingBar.setRating(avgReviews);

                        final Rating rating = new Rating(customerID, rate, comment, currentUserID, ds.getKey(),date);
                        reviewsList.add(rating);
                        displayedList.add(rating);
                        reviewsMap.put(ds.getKey(), rating);
                        //Get restaurant name
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("restaurants/" + restaurantID);
                        customerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                rating.setRestaurantName(dataSnapshot.child("Name").getValue().toString());
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        //Sorting list in terms of data
                        Collections.sort(displayedList,Rating.timeComparator);
                        recyclerViewAdapter.notifyDataSetChanged();
                        show_main_view();

                    }
                }else{
                    //no ratings
                    ratingBar.setRating(0);
                    show_empty_view();
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) {
                if (ds.exists()) {

                    if (ds.hasChild("comment") &&
                            ds.hasChild("customerID") &&
                            ds.hasChild("orderID") &&
                            ds.hasChild("rate") &&
                            ds.hasChild("restaurantID")) {
                        String customerID = ds.child("customerID").getValue().toString();
                        String restaurantID = ds.child("restaurantID").getValue().toString();
                        int rate = Integer.parseInt(ds.child("rate").getValue().toString());
                        final String date = ds.child("date").getValue().toString();
                        String comment = ds.child("comment").getValue().toString();

                        //Compute overall rating of restaurant selected
                        totalStars += rate;
                        totalReviews++;
                        avgReviews = (float) totalStars / totalReviews;
                        ratingBar.setRating(avgReviews);

                        final Rating rating = new Rating(customerID, rate, comment, currentUserID, ds.getKey(), date);
                        //Check if this rating is not already in the collections
                        if(!reviewsMap.containsKey(ds.getKey())){
                            //adding rating to list
                            displayedList.add(rating);
                        }else{
                            for(Rating r : reviewsList){
                                if(r.getOrderID().equals(rating.getOrderID())){
                                    //if is already in the collection, update
                                    r = rating;
                                }
                            }
                        }
                        reviewsMap.put(ds.getKey(), rating);
                        //Get restaurant name
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("restaurants/" + restaurantID);
                        customerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                rating.setRestaurantName(dataSnapshot.child("Name").getValue().toString());
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        Collections.sort(displayedList,Rating.timeComparator);
                        recyclerViewAdapter.notifyDataSetChanged();
                        show_main_view();
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
    /**
     * This method shows the original list of reviews
     */
    private void restoreOriginalList(){
        displayedList.clear();
        displayedList.addAll(reviewsList);
        if(!displayedList.isEmpty())
            show_main_view();
    }
    /**
     * This method removes rating from the displayed list
     * @param rating
     */
    private void removeFromDisplayable(Rating rating){
        displayedList.remove(rating);
        if(displayedList.isEmpty())
            show_empty_view();
        recyclerViewAdapter.notifyDataSetChanged();
    }

    /**
     * This method filter the ratings that doesn't match the constraints
     */
    private void filterList(){
        if(displayedList.isEmpty())
            return;

        Iterator<Rating> iterator= displayedList.iterator();
        while (iterator.hasNext()){
            Rating r = iterator.next();
            if(!isValidToDsplay(r)){
                iterator.remove();
            }
        }
    }

    /**
     * This method check if rating is valid to display i.e. it has a non-empty comment
     * @param rating
     * @return
     */
    private boolean isValidToDsplay(Rating rating){
        return !rating.getComment().equals("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //remove all listener of Firebase
        reviewsReference.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        reviewsReference.removeAllListener();
    }
}
