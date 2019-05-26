package com.mad.poleato.MyReviews;


import android.app.ProgressDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.mad.poleato.Classes.Rating;
import com.mad.poleato.Classes.Restaurant;
import com.mad.poleato.FavoriteRestaurants.FavoriteRestaurantRecyclerViewAdapter;
import com.mad.poleato.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyReviewsFragment extends Fragment {

    private List<Rating> ratingList;
    private MyReviewsRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private DatabaseReference dbReference;

    private Set<String> typesToFilter;

    private View fragView;

    private ProgressDialog progressDialog;
    
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };



    public MyReviewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        typesToFilter = new HashSet<>();
        ratingList = new ArrayList<>();
 //       currDisplayedList = new ArrayList<>();

 //       dbReferenceList= new ArrayList<>();

 //       mAuth = FirebaseAuth.getInstance();
 //       FirebaseUser currentUser = mAuth.getCurrentUser();
 //       currentUserID = currentUser.getUid();

//        if(getActivity() != null)
//            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

//        fillFields();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.fragment_my_reviews, container, false);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv = fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new MyReviewsRecyclerViewAdapter(ratingList);
        rv.setAdapter(recyclerAdapter);

        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(getActivity(), 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);
    }

    private void addToDisplay(Rating r) {
        //add new item to the displayed list
        ratingList.add(r);
    }

    private void removeFromDisplay(Rating r) {

        ratingList.remove(r);
        // simply update. No order is compromised by removing an item
        recyclerAdapter.notifyDataSetChanged();
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
}
