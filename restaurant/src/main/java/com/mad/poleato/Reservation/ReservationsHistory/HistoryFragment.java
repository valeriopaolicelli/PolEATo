package com.mad.poleato.Reservation.ReservationsHistory;

import android.app.ProgressDialog;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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
import com.mad.poleato.R;
import com.mad.poleato.Reservation.Dish;
import com.mad.poleato.Reservation.Reservation;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private  FirebaseAuth mAuth;
    private Toast myToast;
    private String currentUserID;
    private View view;

    private ExpandableListView listView;
    private HistoryExpandableListAdapter listAdapter;
    private Display display;
    private Point size;
    private int width;
    private String localeShort;
    private ProgressDialog progressDialog;
    private DatabaseReference customer; //to retrieve the customer details -> global to handle async behaviour of FB
    private List<String> customerDetails;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);

        /** Calculate position of ExpandableListView indicator. */
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        Locale locale= Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);
        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
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
                OneSignal.setSubscription(false);
                //                OneSignal.sendTag("User_ID", "");
                OneSignal.setSubscription(false);

                /**
                 *  GO TO LOGIN ****
                 */
                Navigation.findNavController(view).navigate(R.id.action_history_id_to_signInActivity);
                getActivity().finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history_reservation_layout, container, false);

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        initData();

        listView = view.findViewById(R.id.historylv);
        /*fix expandablelistview arrow position */
        listView.setIndicatorBounds(width - GetDipsFromPixel(35), width - GetDipsFromPixel(5));
        listAdapter = new HistoryExpandableListAdapter(getActivity(), reservations, listHash, currentUserID);
        listView.setAdapter(listAdapter);

        return view;
    }

    public int GetDipsFromPixel(float pixels) {
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    private void initData(){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                .child(currentUserID).child("history");

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
                Reservation r= null;
                final String order_id, customer_id;
                String note= null;

                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("dishes")
                )
                {

                    order_id = dataSnapshot.getKey();
                    customer_id = dataSnapshot.child("customerID").getValue().toString();
                    final Long dateInMills= Long.parseLong(dataSnapshot.child("date").getValue().toString());
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dateInMills);
                    final String date = formatter.format(calendar.getTime());
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();


                    //Retrieve through customerID the details of the customer
                    customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    readData(new FirebaseCallBack() {
                        @Override
                        public void onCallBack(List<String> customerDetails) {
                            Log.d("Valerio", customerDetails.toString());
                            for(Reservation r : reservations){
                                if(order_id.equals(r.getOrder_id())){
                                    r.setCustomerID(customer_id);
                                    r.setName(customerDetails.get(0));
                                    r.setSurname(customerDetails.get(1));
                                    r.setAddress(customerDetails.get(2));
                                    r.setPhone(customerDetails.get(3));
                                }
                            }

                            listAdapter.notifyDataSetChanged();
                        }
                    });
                    // fields setted to null only because they will be setted later in the call back of FB
                    r = new Reservation(order_id, customer_id,null, null, null, date, time,
                            status, null, totalPrice, localeShort);
                    reservations.add(r);

                    //and for each customer (reservation) retrieve the list of dishes
                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    String nameDish;
                    String foodID;
                    int quantity;
                    Dish d;

                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("selectedQuantity").getValue().toString());
                        note= dish.child("customerNotes").getValue().toString();
                        foodID= dish.child("foodID").getValue().toString();
                        d = new Dish(nameDish, quantity, note, foodID);
                        r.addDishtoReservation(d);
                    }
                    listHash.put(r.getOrder_id(), r.getDishes());
                    if(!listHash.containsKey(order_id)){
                        reservations.add(r);
                    }
                    else{
                        for(Reservation res : reservations)
                            if(res.getOrder_id().equals(order_id))
                                res.setStat(status);
                    }
                    Collections.sort(reservations, Reservation.timeComparator);
                    listAdapter.notifyDataSetChanged();
                    listAdapter.updateReservationList(reservations,listHash);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                Log.d("Valerio", dataSnapshot.getKey());

                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("dishes")
                )
                {
                    final String order_id= dataSnapshot.getKey();
                    final String customer_id= dataSnapshot.child("customerID").getValue().toString();
                    final Long dateInMills= Long.parseLong(dataSnapshot.child("date").getValue().toString());

                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dateInMills);
                    final String date = formatter.format(calendar.getTime());

                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();
                    String note= null;
                    ArrayList<Dish> dishes= new ArrayList<>();

                    //Retrieve through customerID the details of the customer
                    customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    readData(new FirebaseCallBack() {
                        @Override
                        public void onCallBack(List<String> customerDetails) {
                            Log.d("Valerio", customerDetails.toString());
                            for(Reservation r : reservations){
                                if(order_id.equals(r.getOrder_id())){
                                    r.setCustomerID(customer_id);
                                    r.setName(customerDetails.get(0));
                                    r.setSurname(customerDetails.get(1));
                                    r.setAddress(customerDetails.get(2));
                                    r.setPhone(customerDetails.get(3));
                                }
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                    });

                    //and for each customer (reservation) retrieve the list of dishes
                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    String nameDish;
                    String foodID;
                    int quantity;
                    Dish d;

                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("selectedQuantity").getValue().toString());
                        foodID = dish.child("foodID").getValue().toString();
                        note = dish.child("customerNotes").getValue().toString();
                        d = new Dish(nameDish, quantity, note, foodID);

                        dishes.add(d);
                    }

                    Reservation r = new Reservation(order_id, customer_id, null, null, null, date, time,
                            status, null, totalPrice, localeShort);

                    // if the status is changed (onclick listener) the order must change only and not re-added
                    if(!listHash.containsKey(order_id)){
                        reservations.add(r);
                    }

                    listHash.put(order_id, dishes);
                    r.setDishes(dishes);

                    for(Reservation res : reservations)
                        if(res.getOrder_id().equals(order_id))
                            res.setStat(status);

                    listAdapter.notifyDataSetChanged();
                    Collections.sort(reservations, Reservation.timeComparator);
                    listAdapter.updateReservationList(reservations, listHash);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Valerio", dataSnapshot.getKey());
                String order_id= dataSnapshot.getKey();
                for(int i=0; i<reservations.size(); i++)
                    if(reservations.get(i).getOrder_id().equals(order_id)) {
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
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                myToast.setText(databaseError.getMessage());
                myToast.show();
            }
        });
    }

    private void readData(final FirebaseCallBack firebaseCallBack){
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot ds) {
                Log.d("Valerio", ds.getKey());
                String nameCustomer= ds.child("Name").getValue().toString();
                String surnameCustomer= ds.child("Surname").getValue().toString();
                String addressCustomer= ds.child("Address").getValue().toString();
                String phoneCustomer= ds.child("Phone").getValue().toString();

                customerDetails.add(nameCustomer);
                customerDetails.add(surnameCustomer);
                customerDetails.add(addressCustomer);
                customerDetails.add(phoneCustomer);

                firebaseCallBack.onCallBack(customerDetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                myToast.setText(databaseError.getMessage());
                myToast.show();
            }
        };
        customer.addListenerForSingleValueEvent(valueEventListener);
    }

    private interface FirebaseCallBack {
        void onCallBack(List<String> customerDetails);
    }
}
