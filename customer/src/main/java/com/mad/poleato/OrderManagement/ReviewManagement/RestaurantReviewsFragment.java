package com.mad.poleato.OrderManagement.ReviewManagement;


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
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Classes.Rating;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Fragment related to show all reviews of selected restaurants
 *
 */
public class RestaurantReviewsFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;

    private ReviewRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private RatingBar ratingBar;
    private TextView restaurantName;
    private CheckBox onlyCommentCheckBox;
    private String restaurantID;
    private String resName;

    private long totalReviews;
    private float avgReviews;
    private int totalStars;

    private HashMap<String, Rating>reviewsMap;
    private List<Rating>reviewsList;
    private List<Rating>displayedList;

    private ProgressDialog progressDialog;

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    private ImageView empty_view;
    private CardView main_cardview;

    private Boolean already_created; //if this fragment was already created

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

        restaurantID = getArguments().getString("id");
        resName = getArguments().getString("name");

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        DatabaseReference reviewsReference = FirebaseDatabase.getInstance().getReference("restaurants/"+ restaurantID + "/Ratings");
        dbReferenceList.put("ratings", new MyDatabaseReference(reviewsReference));

        if(getActivity() != null){
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        }
        fillFields();

        already_created = false;
    }

    public RestaurantReviewsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurantreviews_fragment_layout, container, false);
        // Inflate the layout for this fragment

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ratingBar = (RatingBar) fragView.findViewById(R.id.rating_bar_avg);
        restaurantName = (TextView) fragView.findViewById(R.id.restaurantName_tv);
        onlyCommentCheckBox = (CheckBox) fragView.findViewById(R.id.checkBoxComments);
        restaurantName.setText(resName);



        rv = (RecyclerView) fragView.findViewById(R.id.reviews_rv);
        rv.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerViewAdapter = new ReviewRecyclerViewAdapter(this.hostActivity,displayedList);
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

        main_cardview = (CardView) fragView.findViewById(R.id.restaurantreviews_cardView);
        empty_view = (ImageView) fragView.findViewById(R.id.reviews_empty_view);

        if(!already_created || (displayedList != null && displayedList.isEmpty()))
            show_empty_view();
        already_created = true;
    }


    private void show_empty_view(){

        main_cardview.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    private void show_main_view(){

        empty_view.setVisibility(View.GONE);
        main_cardview.setVisibility(View.VISIBLE);
    }


    /**
     * Method dedicate to fill the view elements
     * Getting data from database
     */
    public void fillFields(){

        dbReferenceList.get("ratings").setValueListener(new ValueEventListener() {
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

        //Get all ratings of restaurant selected
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

                        //Compute overall rating of restaurant selected
                        totalStars+=rate;
                        totalReviews++;
                        avgReviews = (float)totalStars/totalReviews;
                        ratingBar.setRating(avgReviews);

                        //Creating new rating object and adding it to the collections
                        final Rating rating = new Rating(customerID, rate, comment, restaurantID, ds.getKey(), date);
                        reviewsList.add(rating);
                        displayedList.add(rating);
                        reviewsMap.put(ds.getKey(), rating);

                        //Get customer data
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customers/" + customerID);
                        dbReferenceList.put("customerAdded", new MyDatabaseReference(customerReference));

                        dbReferenceList.get("customerAdded").setSingleValueListener(new ValueEventListener() {
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

                        //Sorting list in terms of data
                        Collections.sort(displayedList,Rating.timeComparator);
                        recyclerViewAdapter.notifyDataSetChanged();
                        show_main_view();

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

                        //Compute overall rating of restaurant selected
                        totalStars += rate;
                        totalReviews++;
                        avgReviews = (float) totalStars / totalReviews;
                        ratingBar.setRating(avgReviews);

                        final Rating rating = new Rating(customerID, rate, comment, restaurantID, ds.getKey(), date);
                        //Check if this rating is not already in the collections
                        if(!reviewsMap.containsKey(ds.getKey())){
                            //adding rating to list
                            displayedList.add(rating);
                        }else{
                            for(Rating r : reviewsList){
                                //if is already in the collection, update
                                if(r.getOrderID().equals(rating.getOrderID())){
                                    r = rating;
                                }
                            }
                        }
                        reviewsMap.put(ds.getKey(), rating);
                        //Get customer data
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("customers/" + customerID);
                        dbReferenceList.put("customerChanged", new MyDatabaseReference(customerReference));

                        dbReferenceList.get("customerChanged").setSingleValueListener(new ValueEventListener() {
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
                        //Sorting list in terms of date
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
        recyclerViewAdapter.notifyDataSetChanged();
        if(displayedList.isEmpty())
            show_empty_view();
    }


    /**
     * This method filter the ratings that doesn't match the constraints
     */
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


    /**
     * This method check if rating is valid to display i.e. it has a non-empty comment
     * @param rating
     * @return
     */
    private boolean isValidToDsplay(Rating rating){
        return !rating.getComment().equals("");
    }

    @Override
    public void onStop() {
        super.onStop();
        //remove all listener of Firebase
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}
