package com.mad.poleato.History;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Classes.Dish;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.NavigatorActivity;
import com.mad.poleato.R;
import com.mad.poleato.Classes.Reservation;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * Fragment that handle all customer's orders
 */
public class Holder_history extends Fragment {

    private ExpandableListView lv;
    private ImageView empty_view;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private String localeShort;
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

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    public Holder_history() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        Locale locale= Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);

// OneSignal is used to send notifications between applications

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

        dbReferenceList = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_holder_history, container, false);


        if (getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        initData();

        lv = view.findViewById(R.id.reservationslv);

        /*fix expandablelistview arrow position */
        lv.setIndicatorBounds(width - GetDipsFromPixel(35), width - GetDipsFromPixel(5));

        listAdapter = new ReservationExpandableListAdapter(getActivity(), reservations, listHash, currentUserID);
        lv.setAdapter(listAdapter);

        empty_view = (ImageView) view.findViewById(R.id.history_empty_view);
        show_empty_view();

        return view;
    }

    private void show_history_view(){

        empty_view.setVisibility(View.GONE);
        lv.setVisibility(View.VISIBLE);
    }

    private void show_empty_view(){

        lv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    /**
     * Method to populate the view's element
     */
    private void initData() {
        reservations = new ArrayList<>();
        listHash = new HashMap<>();

        //get all customer reservations
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("customers")
                .child(currentUserID).child("reservations");

        dbReferenceList.put("reservation", new MyDatabaseReference(reference));

        dbReferenceList.get("reservation").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);
            }
        });

        dbReferenceList.get("reservation").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("restaurantName") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("dishes") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("reviewFlag") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en")) {

                    String nameDish;
                    int quantity;
                    String note;
                    List<Dish> dishes = new ArrayList<>();
                    Reservation r;

                    /*
                     *   Retrieve all fields
                     */
                    final String orderID = dataSnapshot.child("orderID").getValue().toString();
                    final String restaurantName = dataSnapshot.child("restaurantName").getValue().toString();
                    final Long dateInMills = Long.parseLong(dataSnapshot.child("date").getValue().toString());
                    Boolean reviewFlag = Boolean.parseBoolean(dataSnapshot.child("reviewFlag").getValue().toString());
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();

                    //Convert date
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dateInMills);


                    final String date = formatter.format(calendar.getTime());

                    final String time = dataSnapshot.child("time").getValue().toString();
                    final String totalPrice = dataSnapshot.child("totalPrice").getValue().toString();
                    String restaurantID = dataSnapshot.child("restaurantID").getValue().toString();
                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    //get all dishes details
                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        note = dish.child("notes").getValue().toString();
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Non hai lasciato commenti";
                            else
                                note= "Without comments";
                        }
                        dishes.add(new Dish(nameDish, quantity, note));
                    }
                    r = new Reservation(orderID, restaurantName, date, time, totalPrice);
                    r.setDishes(dishes);
                    r.setRestaurantID(restaurantID);
                    r.setReviewFlag(reviewFlag);
                    r.setStatus(status);
                    /*
                     * Update the expandable list adapter
                     */
                    reservations.add(r);
                    listHash.put(r.getOrderID(), r.getDishes());
                    Collections.sort(reservations, Reservation.timeComparator);
                    listAdapter.notifyDataSetChanged();
                    show_history_view();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.hasChild("orderID") &&
                        dataSnapshot.hasChild("restaurantName") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("dishes") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("reviewFlag") &&
                        dataSnapshot.hasChild("name") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en")) {

                    String nameDish;
                    int quantity;
                    String note;
                    List<Dish> dishes = new ArrayList<>();
                    Reservation r;

                    /*
                     *   Retrieve all fields
                     */
                    final String orderID = dataSnapshot.child("orderID").getValue().toString();
                    final String restaurantName = dataSnapshot.child("restaurantName").getValue().toString();
                    final String date = dataSnapshot.child("date").getValue().toString();
                    final String time = dataSnapshot.child("time").getValue().toString();
                    final String totalPrice = dataSnapshot.child("totalPrice").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();

                    Boolean reviewFlag = Boolean.parseBoolean(dataSnapshot.child("reviewFlag").getValue().toString());
                    String restaurantID = dataSnapshot.child("restaurantID").getValue().toString();

                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        note = dish.child("notes").getValue().toString();
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Non hai lasciato commenti";
                            else
                                note= "Without comments";
                        }
                        dishes.add(new Dish(nameDish, quantity, note));
                    }
                    r = new Reservation(orderID, restaurantName, date, time, totalPrice);
                    r.setDishes(dishes);
                    r.setRestaurantID(restaurantID);
                    r.setReviewFlag(reviewFlag);
                    r.setStatus(status);
                    /*
                     * Update the expandable list adapter
                     */
                    listHash.put(r.getOrderID(), r.getDishes());
                    if (!listHash.containsKey(orderID)) {
                        reservations.add(r);
                    } else {
                        for (Reservation reservation : reservations) {
                            if (reservation.getOrderID().equals(r.getOrderID())) {
                                reservation.setDishes(dishes);
                                reservation.setRestaurantID(restaurantID);
                                reservation.setReviewFlag(reviewFlag);
                                reservation.setStatus(status);
                            }
                        }
                    }
                    Collections.sort(reservations, Reservation.timeComparator);
                    listAdapter.updateReservations(reservations);
                    listAdapter.notifyDataSetChanged();
                    show_history_view();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String order_id = dataSnapshot.getKey();
                for (int i = 0; i < reservations.size(); i++)
                    if (reservations.get(i).getOrderID().equals(order_id)) {
                        reservations.remove(i);
                        break;
                    }
                listHash.remove(order_id);
                listAdapter.notifyDataSetChanged();
                if(listHash.isEmpty())
                    show_empty_view();
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

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            NavigatorActivity.hideKeyboard(getActivity());
        }
    }
}
