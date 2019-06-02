package com.mad.poleato.Reservation;

import android.app.ProgressDialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Point;
import android.view.Display;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;

import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import java.util.List;
import java.util.Locale;

/**
 * This fragment shows all the reservations received from the customers
 * Shows new and pending until they'll be confirmed by the restaurateur
 */
public class ReservationFragment extends Fragment {

    Toast myToast;
    private ImageView empty_view;
    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();
    private List<String> customerDetails;
    private int width;

    private String currentUserID;
    private String localeShort;

    private GoogleSignInClient mGoogleSignInClient;

    private ProgressDialog progressDialog;

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    public ReservationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        Locale locale= Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);
        /** Calculate position of ExpandableListView indicator. */
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();


        /** GoogleSignInOptions */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /** Build a GoogleSignInClient with the options specified by gso. */
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new HashMap<>();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout, container, false);


        lv = view.findViewById(R.id.reservationslv);
        empty_view = (ImageView) view.findViewById(R.id.reservation_empty_view);

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        initData();

        /*fix expandablelistview arrow position */
        lv.setIndicatorBounds(width - GetDipsFromPixel(35), width - GetDipsFromPixel(5));
        listAdapter = new ReservationExpandableListAdapter(getActivity(), reservations, listHash, currentUserID);
        lv.setAdapter(listAdapter);
        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                Button b = (Button) view.findViewById(R.id.myButton);
                if (!lv.isGroupExpanded(i)) {
                    b.setVisibility(View.VISIBLE);
                } else
                    b.setVisibility(View.GONE);
                return false;
            }
        });
        show_empty_view();

        return view;
    }

    /**
     * This method shows an empty view if the collection are empty
     */
    private void show_empty_view(){

        lv.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }


    private void show_resevation_view(){

        empty_view.setVisibility(View.GONE);
        lv.setVisibility(View.VISIBLE);
    }

    /**
     * Needed to place the arrow of expandable list
     * given the size of the screen
     * @param pixels
     * @return
     */
    public int GetDipsFromPixel(float pixels) {
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    /**
     * Method that fill the view elements
     */
    private void initData() {
        reservations = new ArrayList<>();
        listHash = new HashMap<>();
        customerDetails= new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                .child(currentUserID).child("reservations");
        dbReferenceList.put("reservations", new MyDatabaseReference(reference));

        /**
         * Listener called after ChildListener that will dismiss progress dialog
         */
        dbReferenceList.get("reservations").setValueListener(new ValueEventListener() {
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
        //reference to the reservation of the restaurant
        dbReferenceList.get("reservations").setChildListener( new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Reservation r= null;
                final String order_id, customer_id;
                String note= null;
                //Check if all fields are present on database
                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en") &&
                        ((
                                (dataSnapshot.child("status/it").getValue().toString().equals("Nuovo ordine") &&
                                        dataSnapshot.child("status/en").getValue().toString().equals("New order")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("Preparazione") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Cooking")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("In consegna") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Delivering")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("Consegnato") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Delivered"))
                        ) && dataSnapshot.hasChild("date") &&
                                dataSnapshot.hasChild("dishes")
                        )
                )
                {
                    //retrieve the customer (reservation) details

                    order_id = dataSnapshot.getKey();
                    customer_id = dataSnapshot.child("customerID").getValue().toString();
                    final Long dateInMills= Long.parseLong(dataSnapshot.child("date").getValue().toString());
                    //Date is in millis on database, converting to normal format
                    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dateInMills);
                    final String date = formatter.format(calendar.getTime());
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();

                    //Retrieve through customerID the details of the customer
                    DatabaseReference customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    dbReferenceList.put("customersAdded", new MyDatabaseReference(customer));

                    readData(new FirebaseCallBack() {
                        @Override
                        public void onCallBack(List<String> customerDetails) {
                            Log.d("Valerio", customerDetails.toString());
                            for(Reservation r : reservations){
                                if(order_id.equals(r.getOrder_id())){
                                    r.setName(customerDetails.get(0));
                                    r.setSurname(customerDetails.get(1));
                                    r.setAddress(customerDetails.get(2));
                                    r.setPhone(customerDetails.get(3));
                                }
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                    }, "customersAdded");


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

                    //Getting customer's dishes details
                    for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                        nameDish = dish.child("name").getValue().toString();
                        quantity = Integer.parseInt(dish.child("selectedQuantity").getValue().toString());
                        note= dish.child("customerNotes").getValue().toString();
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Nessuna nota dal cliente";
                            else
                                note= "No customer notes";
                        }

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
                    //Sort reservation based on date
                    Collections.sort(reservations, Reservation.timeComparator);
                    //Add new check state to the reservation
                    listAdapter.addCheckState(false);
                    //update collection of the adapter
                    listAdapter.updateReservationList(reservations,listHash);

                    listAdapter.notifyDataSetChanged();
                    show_resevation_view();
                }
            }

            /**
             * Method that resembles the onChildAdded method, see that for more details
             * @param dataSnapshot
             * @param s
             */
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("Valerio", dataSnapshot.getKey());

                if(dataSnapshot.hasChild("customerID") &&
                        dataSnapshot.hasChild("restaurantID") &&
                        dataSnapshot.hasChild("totalPrice") &&
                        dataSnapshot.hasChild("time") &&
                        dataSnapshot.hasChild("status") &&
                        dataSnapshot.child("status").hasChild("it") &&
                        dataSnapshot.child("status").hasChild("en") &&
                        ((
                                (dataSnapshot.child("status/it").getValue().toString().equals("Nuovo ordine") &&
                                        dataSnapshot.child("status/en").getValue().toString().equals("New order")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("Preparazione") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Cooking")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("In consegna") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Delivering")) ||
                                        (dataSnapshot.child("status/it").getValue().toString().equals("Consegnato") &&
                                                dataSnapshot.child("status/en").getValue().toString().equals("Delivered"))
                          ) && dataSnapshot.hasChild("date") &&
                                dataSnapshot.hasChild("dishes")
                        )
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
                    DatabaseReference customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
                    dbReferenceList.put("customersChanged", new MyDatabaseReference(customer));

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
                    }, "customersChanged");

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
                        if(note.equals("")){
                            if(localeShort.equals("it"))
                                note= "Nessun commento dal cliente";
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
                    //set dishes collection to reservation
                    r.setDishes(dishes);

                    for(Reservation res : reservations)
                            if(res.getOrder_id().equals(order_id))
                                res.setStat(status);

                    listAdapter.notifyDataSetChanged();
                    Collections.sort(reservations, Reservation.timeComparator);
                    listAdapter.updateReservationList(reservations, listHash);
                    listAdapter.addCheckState(false);
                    show_resevation_view();
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


    /**
     * Method to save persistence rotating the phone
     * @param outState
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> statusPersistence = new ArrayList<>();
        ArrayList<String> textButtonPersistence = new ArrayList<>();
        if(listAdapter != null) {
            for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                View v = listAdapter.getGroupView(i, false, null, lv);
                TextView status = v.findViewById(R.id.tvStatusField);
                Button button = v.findViewById(R.id.myButton);
                statusPersistence.add(i, status.getText().toString());
                textButtonPersistence.add(i, button.getText().toString());
            }
            outState.putStringArrayList("Status_Persistence", statusPersistence);
            outState.putStringArrayList("Button_Text_Persistence", textButtonPersistence);
        }
    }

    /**
     * Method to save persistence rotating the phone
     * @param savedInstanceState
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ArrayList<String> statusPersistence;
        ArrayList<String> buttonTextPersistence;
        if (savedInstanceState != null) {
            statusPersistence = savedInstanceState.getStringArrayList("Status_Persistence");
            buttonTextPersistence = savedInstanceState.getStringArrayList("Button_Text_Persistence");
            if (statusPersistence != null && buttonTextPersistence != null)
                if (statusPersistence.size() > 0 && buttonTextPersistence.size() > 0) {
                    for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                        reservations.get(i).setStat(statusPersistence.get(i));
                        reservations.get(i).setButtonText(buttonTextPersistence.get(i));
                    }
                }


        }
    }

    /**
     * Firebase callback to retrieve datas about customer
     * @param firebaseCallBack
     * @param key
     */
    private void readData(final FirebaseCallBack firebaseCallBack, String key){
        ValueEventListener valueEventListener= new ValueEventListener() {
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
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        for(MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}