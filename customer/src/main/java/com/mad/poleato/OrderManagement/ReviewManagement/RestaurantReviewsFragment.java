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
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mad.poleato.R;

import java.util.ArrayList;
import java.util.HashMap;
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
    private TextView restaurantName;

    private String restaurantID;
    private String resName;
    private long totalReviews;
    private float avgReviews;
    DatabaseReference reviewsReference;

    private HashMap<String, Rating>reviewsMap;
    private List<Rating>reviewsList;

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
        restaurantID = getArguments().getString("id");
        resName = getArguments().getString("name");

        reviewsReference = FirebaseDatabase.getInstance().getReference("restaurants/"+ restaurantID + "/Ratings");

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

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ratingBar = (RatingBar) fragView.findViewById(R.id.rating_bar_avg);
        restaurantName = (TextView) fragView.findViewById(R.id.restaurantName_tv);
        restaurantName.setText("Reviews for "+ resName);



        rv = (RecyclerView) fragView.findViewById(R.id.reviews_rv);
        rv.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerViewAdapter = new ReviewRecyclerViewAdapter(this.hostActivity,reviewsList);
        rv.setAdapter(recyclerViewAdapter);


    }

    public void fillFields(){

        reviewsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);

            }
        });


        reviewsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {
                if (ds.exists()) {
                    final String[] customerName = new String[1];
                    final String[] customerSurname = new String[1];

                    //Compute avg rating
                    int totalStars = 0;
                    avgReviews = (float) totalStars / totalReviews;
                    ratingBar.setRating(avgReviews);
                    if (ds.hasChild("comment") &&
                            ds.hasChild("customerID") &&
                            ds.hasChild("orderID") &&
                            ds.hasChild("rate") &&
                            ds.hasChild("restaurantID")) {
                        String customerID = ds.child("customerID").getValue().toString();
                        int rate = Integer.parseInt(ds.child("rate").getValue().toString());
                        final String date = ds.child("date").getValue().toString();
                        String comment = ds.child("comment").getValue().toString();



                        //Get customer data
                        DatabaseReference customerReference = FirebaseDatabase.getInstance().getReference("Customers/" + customerID);
                        customerReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                customerName[0] = dataSnapshot.child("Name").getValue().toString();
                                customerSurname[0] = dataSnapshot.child("Surname").getValue().toString();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        Rating rating = new Rating(customerID, rate, comment, restaurantID, ds.getKey());
                        rating.setCustomerData(customerName[0] + " " + customerSurname[0]);
                        reviewsList.add(rating);
                        reviewsMap.put(customerID, rating);
                        recyclerViewAdapter.notifyDataSetChanged();

                    }
                }
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
}
