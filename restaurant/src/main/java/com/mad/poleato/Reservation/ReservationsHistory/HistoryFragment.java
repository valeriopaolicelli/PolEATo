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
import android.widget.ImageView;
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
import com.mad.poleato.MyDatabaseReference;
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
    private ImageView empty_view;
    private HistoryExpandableListAdapter listAdapter;
    private Display display;
    private Point size;
    private int width;
    private String localeShort;
    private ProgressDialog progressDialog;
    private List<String> customerDetails;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();

    private HashMap<String, MyDatabaseReference> dbReferenceList;
    private int indexCustomerAdded;
    private int indexCustomerChanged;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        dbReferenceList = new HashMap<>();
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

        empty_view = (ImageView) view.findViewById(R.id.history_empty_view);
        show_empty_view();

        return view;
    }


    private void show_empty_view() {

        listView.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }


    private void show_history_view(){

        empty_view.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }


    public int GetDipsFromPixel(float pixels) {
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    private void initData(){

        reservations = new ArrayList<>();
        listHash = new HashMap<>();
        customerDetails= new ArrayList<>();

        /*
         * check if there are some orders delivered or also paid in the reservation tab
         * in that case, update the reservation list, remove this orders and add them to history
         */
        DatabaseReference referenceReservation= FirebaseDatabase.getInstance()
                .getReference("restaurants/"+currentUserID+"/reservations");
        dbReferenceList.put("reservations", new MyDatabaseReference(referenceReservation));

        dbReferenceList.get("reservations").setValueListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot dataSnapshotReservation : dataSnapshot.getChildren()){
                                if(dataSnapshotReservation.hasChild("status") &&
                                        dataSnapshotReservation.hasChild("status/it") &&
                                        dataSnapshotReservation.hasChild("status/en") &&
                                        dataSnapshotReservation.hasChild("customerID") &&
                                        dataSnapshotReservation.hasChild("date") &&
                                        dataSnapshotReservation.hasChild("dishes") &&
                                        dataSnapshotReservation.hasChild("restaurantID") &&
                                        dataSnapshotReservation.hasChild("time") &&
                                        dataSnapshotReservation.hasChild("totalPrice")){

                                    String statusIT= dataSnapshotReservation.child("status/it").getValue().toString();
                                    String statusEN= dataSnapshotReservation.child("status/en").getValue().toString();

                                    if((statusEN.equals("Delivered") && statusIT.equals("Consegnato")) ||
                                            (statusEN.equals("Failed") && statusIT.equals("Fallito")) ||
                                            (statusEN.equals("Rejected") && statusIT.equals("Rifiutato"))){
                                        String orderID= dataSnapshotReservation.getKey();
                                        String customerID= dataSnapshotReservation.child("customerID").getValue().toString();
                                        String time= dataSnapshotReservation.child("time").getValue().toString();
                                        String totalPrice= dataSnapshotReservation.child("totalPrice").getValue().toString();
                                        final Long dateInMills= Long.parseLong(dataSnapshotReservation.child("date").getValue().toString());

                                        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(dateInMills);
                                        final String date = formatter.format(calendar.getTime());

                                        List<Dish> dishes= new ArrayList<>();
                                        String nameDish, foodID, note;
                                        int quantity;
                                        Dish d;
                                        for(DataSnapshot dataSnapshotDishes : dataSnapshotReservation.child("dishes").getChildren()){
                                            nameDish = dataSnapshotDishes.child("name").getValue().toString();
                                            quantity = Integer.parseInt(dataSnapshotDishes.child("selectedQuantity").getValue().toString());
                                            foodID = dataSnapshotDishes.child("foodID").getValue().toString();
                                            note = dataSnapshotDishes.child("customerNotes").getValue().toString();
                                            d = new Dish(nameDish, quantity, note, foodID);

                                            dishes.add(d);
                                        }

                                        //Adding reservation to History
                                        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("restaurants/"+currentUserID+"/History");
                                        dbReference.child(orderID).child("customerID").setValue(customerID);
                                        dbReference.child(orderID).child("date").setValue(date);
                                        dbReference.child(orderID).child("time").setValue(time);
                                        dbReference.child(orderID).child("totalPrice").setValue(totalPrice);
                                        dbReference.child(orderID).child("status/it").setValue(statusIT);
                                        dbReference.child(orderID).child("status/en").setValue(statusEN);
                                        dbReference.child(orderID).child("dishes").setValue(dishes);

                                        //Delete reservation from pending reservations
                                        FirebaseDatabase.getInstance().getReference("restaurants/"+currentUserID+"/reservations/"+orderID).removeValue();

                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        /*
         * retrieve the history of restaurant:
         * there are all orders delivered, paid and rejected
         */

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                .child(currentUserID).child("History");
        dbReferenceList.put("history", new MyDatabaseReference(reference));

        dbReferenceList.get("history").setValueListener(new ValueEventListener() {
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

        dbReferenceList.get("history").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Reservation r= null;
                final String order_id, customer_id;
                String note= null;

                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.hasChild("status/it") &&
                        dataSnapshot.hasChild("status/en") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("dishes")
                )
                {
                    order_id = dataSnapshot.getKey();
                    customer_id = dataSnapshot.child("customerID").getValue().toString();
                    final String date = dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status/"+localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();


                    //Retrieve through customerID the details of the customer
                    DatabaseReference customerReference= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    dbReferenceList.put("customerAdded", new MyDatabaseReference(customerReference));

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
                    }, "customerAdded");
                    // fields setted to null only because they will be setted later in the call back of FB
                    r = new Reservation(order_id, customer_id,null, null, null, date, time,
                            status, null, totalPrice, localeShort);

                    //and for each customer (reservation) retrieve the list of dishes
                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    String nameDish;
                    String foodID;
                    int quantity;
                    Dish d;

                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        note= dish.child("notes").getValue().toString();
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Nessuna nota dal cliente";
                            else
                                note= "No customer notes";
                        }

                        foodID= dish.child("id").getValue().toString();
                        d = new Dish(nameDish, quantity, note, foodID);
                        r.addDishtoReservation(d);
                    }

                    if(!listHash.containsKey(order_id)){
                        reservations.add(r);
                    }
                    else{
                        for(Reservation res : reservations)
                            if(res.getOrder_id().equals(order_id))
                                res.setStat(status);
                    }
                    listHash.put(r.getOrder_id(), r.getDishes());

                    Collections.sort(reservations, Reservation.timeComparatorReverse);
                    listAdapter.notifyDataSetChanged();
                    listAdapter.updateReservationList(reservations,listHash);
                    show_history_view();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                Log.d("Valerio", dataSnapshot.getKey());

                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.hasChild("status/it") &&
                        dataSnapshot.hasChild("status/en") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("date") &&
                        dataSnapshot.hasChild("dishes")
                )
                {
                    final String order_id= dataSnapshot.getKey();
                    final String customer_id= dataSnapshot.child("customerID").getValue().toString();
                    final String date = dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status/"+localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();
                    String note= null;
                    ArrayList<Dish> dishes= new ArrayList<>();

                    //Retrieve through customerID the details of the customer
                    DatabaseReference customerChanged= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    dbReferenceList.put("customerChanged", new MyDatabaseReference(customerChanged));

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
                    }, "customerChanged");

                    //and for each customer (reservation) retrieve the list of dishes
                    DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                    String nameDish;
                    String foodID;
                    int quantity;
                    Dish d;

                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("quantity").getValue().toString());
                        foodID = dish.child("id").getValue().toString();
                        note = dish.child("notes").getValue().toString();
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Nessuna nota dal cliente";
                            else
                                note= "No customer notes";
                        }

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
                    Collections.sort(reservations, Reservation.timeComparatorReverse);
                    listAdapter.updateReservationList(reservations, listHash);
                    show_history_view();
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
                if(listHash.isEmpty())
                    show_empty_view();
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

    private void readData(final FirebaseCallBack firebaseCallBack, String key){
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
        dbReferenceList.get(key).setSingleValueListener(valueEventListener);
    }

    private interface FirebaseCallBack {
        void onCallBack(List<String> customerDetails);
    }

    @Override
    public void onStop() {
        super.onStop();
        for(MyDatabaseReference my_ref : dbReferenceList.values()){
            my_ref.removeAllListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference my_ref : dbReferenceList.values()){
            my_ref.removeAllListener();
        }
    }
}