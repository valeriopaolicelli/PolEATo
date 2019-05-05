package com.mad.poleato;


import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class Holder_history extends Fragment {

    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();
    private View view;
    private Display display;
    private Point size;
    private int width;

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private String currentUserID;
    private FirebaseAuth mAuth;


    public Holder_history() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;
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

                /**
                 *  GO TO LOGIN ****
                 */

                Navigation.findNavController(view).navigate(R.id.action_holder_history_id_to_signInActivity);
                getActivity().finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_holder_history, container, false);
        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        initData();

        lv = view.findViewById(R.id.reservationslv);

        /*fix expandablelistview arrow position */
        lv.setIndicatorBounds(width - GetDipsFromPixel(35), width - GetDipsFromPixel(5));

        listAdapter = new ReservationExpandableListAdapter(getActivity(), reservations, listHash, currentUserID);
        lv.setAdapter(listAdapter);

        return view;
    }

    private void initData() {
        reservations = new ArrayList<>();
        listHash = new HashMap<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customers")
                .child(currentUserID).child("reservations");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.hasChild("orderID") &&
                    dataSnapshot.hasChild("restaurantName") &&
                    dataSnapshot.hasChild("date") &&
                    dataSnapshot.hasChild("time") &&
                    dataSnapshot.hasChild("totalPrice") &&
                    dataSnapshot.hasChild("dishes")){

                    String nameDish;
                    int quantity;
                    String note;
                    List<Dish> dishes= new ArrayList<>();
                    Reservation r;

                    /*
                     *   Retrieve all fields
                     */
                    final String orderID= dataSnapshot.child("orderID").getValue().toString();
                    final String restaurantName= dataSnapshot.child("restaurantName").getValue().toString();
                    final String date= dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();

                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        note= dish.child("notes").getValue().toString();
                        dishes.add(new Dish(nameDish, quantity, note));
                    }
                    r= new Reservation(orderID, restaurantName, date, time, totalPrice);
                    r.setDishes(dishes);

                    /*
                     * Update the expandable list adapter
                     */
                    reservations.add(r);
                    listHash.put(r.getOrderID(), r.getDishes());
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("restaurantName") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("dishes")){

                    String nameDish;
                    int quantity;
                    String note;
                    List<Dish> dishes= new ArrayList<>();
                    Reservation r;

                    /*
                     *   Retrieve all fields
                     */
                    final String orderID= dataSnapshot.child("orderID").getValue().toString();
                    final String restaurantName= dataSnapshot.child("restaurantName").getValue().toString();
                    final String date= dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();

                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        note= dish.child("notes").getValue().toString();

                        dishes.add(new Dish(nameDish, quantity, note));
                    }
                    r= new Reservation(orderID, restaurantName, date, time, totalPrice);
                    r.setDishes(dishes);

                    /*
                     * Update the expandable list adapter
                     */
                    listHash.put(r.getOrderID(), r.getDishes());
                    if(!listHash.containsKey(orderID)){
                        reservations.add(r);
                    }
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String order_id= dataSnapshot.getKey();
                for(int i=0; i<reservations.size(); i++)
                    if(reservations.get(i).getOrderID().equals(order_id)) {
                        reservations.remove(i);
                        break;
                    }
                listHash.remove(order_id);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("Valerio", dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
            }
        });
    }

    public int GetDipsFromPixel(float pixels) {
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

}
