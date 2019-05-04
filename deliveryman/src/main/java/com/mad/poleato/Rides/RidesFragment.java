package com.mad.poleato.Rides;


import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.FirebaseData.MyFirebaseData;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class RidesFragment extends Fragment {

    private Toast myToast;

    private Activity hostActivity;
    private View fragView;
    private RidesRecyclerViewAdapter ridesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private HashMap<String, Ride> rideMap;
    private List<Ride> rideList;
    private List<Ride> currDisplayedList; //list of filtered elements displayed on the screen
    private MyViewModel model;
    private MyFirebaseData myFirebaseData;

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if (hostActivity != null) {
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        rideList = new ArrayList<>();
        rideMap = new HashMap<>();

        if (getActivity() != null) {
            //TODO: update strings.xml
            progressDialog = ProgressDialog.show(getActivity(), "", "Loading");
        }

        /** Listeners to update UI Expandable list from VIEW_MODEL list child */
        model = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model.getListR().observe(this, new Observer<HashMap<String, Ride>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, Ride> stringRideHashMap) {
                if(stringRideHashMap.values() != null)
                    ridesAdapter.setAllRiders(new ArrayList<Ride>(stringRideHashMap.values()) );
            }
        });

        myFirebaseData = new MyFirebaseData(getActivity(), progressDialog);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.popup_account_settings, menu);
        menu.findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //logout
                Log.d("matte", "Logout");
                FirebaseAuth.getInstance().signOut();
                //Intent myIntent = new Intent(NavigatorActivity.this, SignInActivity.class);
                //NavigatorActivity.this.startActivity(myIntent);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.ride_recyclerview, container, false);

        rv = (RecyclerView) fragView.findViewById(R.id.rides_recyclerview);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.ridesAdapter = new RidesRecyclerViewAdapter(this.hostActivity);
        rv.setAdapter(ridesAdapter);
        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myFirebaseData.fillFieldsRiders();
    }



    private void addToDisplay(Ride r) {
        //add new item to the displayed list
        currDisplayedList.add(r);
        //notify the adapter to show it by keeping the actual order
        ridesAdapter.notifyDataSetChanged();
    }

    private void removeFromDisplay(Ride r) {

        currDisplayedList.remove(r);
        // simply update. No order is compromised by removing an item
        ridesAdapter.notifyDataSetChanged();
    }

}
