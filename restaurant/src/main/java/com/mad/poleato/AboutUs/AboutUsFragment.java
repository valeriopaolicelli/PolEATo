package com.mad.poleato.AboutUs;


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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.DailyOffer.Food;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.mad.poleato.TimeSlot;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutUsFragment extends Fragment {

    private Toast myToast;
    private Activity hostActivity;
    private View fragView;
    private PopularFoodsRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private TextView popularTiming;

    private String currentUserID;
    private FirebaseAuth mAuth;

    private Map<String, MyDatabaseReference> dbReferenceList;
    private List<Food> mostPopularFoods;
    private HashMap<String, Food> mapMostPopularFoods;
    private HashMap<TimeSlot, Integer> mapMostPopularTime;

    private ProgressDialog progressDialog;

    public AboutUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.fragment_about_us, container, false);
        return fragView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new HashMap<>();
        mostPopularFoods= new ArrayList<>();
        mapMostPopularFoods= new HashMap<>();
        mapMostPopularTime= new HashMap<>();

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(hostActivity != null)
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        popularTiming= (TextView) fragView.findViewById(R.id.popular_timing_tv);

        rv = (RecyclerView) fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new PopularFoodsRecyclerViewAdapter(this.hostActivity, this.mostPopularFoods);
        rv.setAdapter(recyclerAdapter);

        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        findMostPopularTiming();
        findMostPopularFoods();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        /** Inflate the menu; this adds items to the action bar if it is present.*/
        inflater.inflate(R.menu.aboutus_menu, menu);

        /** Button to show map */
        menu.findItem(R.id.aboutus_id).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                /**
                 * GO FROM STATISTICS TO REVIEWS
                 */
                Navigation.findNavController(fragView).navigate(R.id.action_aboutus_id_to_reviews_id);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    public void findMostPopularTiming(){

        //history reference
        dbReferenceList.put("history", new MyDatabaseReference(FirebaseDatabase.getInstance()
                                            .getReference("restaurants/"+currentUserID+"/History")));
        dbReferenceList.get("history").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // fill the map with the slots and counter of time popularity to 0
                fillMapSlots();
                TimeSlot popularTimeSlot= null;
                int max= 0;
                for(DataSnapshot reservationReference : dataSnapshot.getChildren()) {
                    String time = reservationReference.child("time").getValue().toString();
                    for(TimeSlot t : mapMostPopularTime.keySet()){
                        if(t.inSlot(time)) {
                            mapMostPopularTime.put(t, mapMostPopularTime.get(t) + 1);
                            if(mapMostPopularTime.get(t) > max){
                                max = mapMostPopularTime.get(t);
                                popularTimeSlot= t;
                            }
                        }
                    }
                }
                popularTiming.setText(popularTimeSlot.getSlot());

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    public void findMostPopularFoods(){
        /**
         * Find the most popular foods:
         * scan all food of its own menu, compute the average:
         * (sum popularity counter of each food) / number of foods,
         * where the popularity counter is a counter updated every time one customer order that food.
         *
         * Then, all foods with popularity counter greater or equal than average, are shown.
         */

        // here compute the average

        dbReferenceList.put("menu", new MyDatabaseReference(FirebaseDatabase.getInstance()
                                            .getReference("restaurants/"+currentUserID+"/Menu")));
        dbReferenceList.get("menu").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long sum = 0;
                long numberOfFood= dataSnapshot.getChildrenCount();
                double popularityAverage; // average of popularity counter of all foods in menu

                for(DataSnapshot foodReference : dataSnapshot.getChildren()){
                    sum += Integer.parseInt(foodReference.child("PopularityCounter").getValue().toString());
                }

                popularityAverage= sum/numberOfFood;

                // here take the foods that exceed the average threshold and populate the recycleViewAdapter
                for(DataSnapshot foodReference : dataSnapshot.getChildren()){
                    long popularityCounter= Integer.parseInt(foodReference.child("PopularityCounter").getValue().toString());
                    if(popularityCounter >= popularityAverage){
                        String name =  foodReference.child("Name").getValue().toString();
                        String description = foodReference.child("Description").getValue().toString();
                        String price = foodReference.child("Price").getValue().toString();
                        String id = foodReference.getKey();

                        Food f = new Food(id, null, name, description, Double.parseDouble(price), 0);
                        if(!mapMostPopularFoods.containsKey(id))
                            mostPopularFoods.add(f);
                        mapMostPopularFoods.put(id, f);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                }

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    public void fillMapSlots(){
        /**
         *  fill the map with time slot of 1 hour
         *  from 00:00 - 01:00 to 23:00 - 24:00
         */
        for(int i=0; i < 24; i++){
            String start = (i<10 ? "0" : "") + i + ":00";
            String end = (i+1<10 ? "0" : "") + (i+1) + ":00";
            mapMostPopularTime.put(new TimeSlot(start, end), 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }
}
