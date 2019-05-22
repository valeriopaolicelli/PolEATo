package com.mad.poleato.Reservation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import android.graphics.Point;
import android.view.Display;
import android.widget.Button;
import android.widget.ExpandableListView;
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

import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.NavigatorActivity;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;
import com.mad.poleato.SignInActivity;
import com.mad.poleato.SignUpActivity;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link ReservationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReservationFragment extends Fragment {

    Toast myToast;
    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();
    private List<String> customerDetails;
    private View view;
    private Display display;
    private Point size;
    private int width;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private String localeShort;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private DatabaseReference customer; //to retrieve the customer details -> global to handle async behaviour of FB
    private int indexCustomerAdded;
    private int indexCustomerChanged;

    private List<MyDatabaseReference> dbReferenceList;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    public ReservationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReservationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReservationFragment newInstance(String param1, String param2) {
        ReservationFragment fragment = new ReservationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        Locale locale= Locale.getDefault();
        localeShort = locale.toString().substring(0, 2);
        /** Calculate position of ExpandableListView indicator. */
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.reservation_frag_layout, container, false);

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        initData();

        lv = view.findViewById(R.id.reservationslv);
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
        return view;
    }

    public int GetDipsFromPixel(float pixels) {
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    private void initData() {
        reservations = new ArrayList<>();
        listHash = new HashMap<>();
        customerDetails= new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                .child(currentUserID).child("reservations");
        dbReferenceList.add(new MyDatabaseReference(reference));
        final int indexReference= dbReferenceList.size()-1;

        ValueEventListener valueEventListener;

        dbReferenceList.get(indexReference).getReference().addValueEventListener(valueEventListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                listAdapter.addCheckState(false);

                handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                handler.sendEmptyMessage(0);
            }
        });
        dbReferenceList.get(indexReference).setValueListener(valueEventListener);


        ChildEventListener childEventListener;
        dbReferenceList.get(indexReference).getReference().addChildEventListener(childEventListener= new ChildEventListener() {
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
                    //retrieve the customer (reservation) details

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
                    dbReferenceList.add(new MyDatabaseReference(customer));
                    indexCustomerAdded= dbReferenceList.size()-1;

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
                    }, indexCustomerAdded);


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
                    listAdapter.addCheckState(false);
                    listAdapter.notifyDataSetChanged();
                    listAdapter.updateReservationList(reservations,listHash);
                }
            }

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
                    dbReferenceList.add(new MyDatabaseReference(customer));
                    indexCustomerChanged= dbReferenceList.size()-1;

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
                    }, indexCustomerChanged);

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
                    listAdapter.addCheckState(false);
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
        dbReferenceList.get(indexReference).setChildListener(childEventListener);

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> statusPersistence = new ArrayList<>();
        ArrayList<String> textButtonPersistence = new ArrayList<>();

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

    private void readData(final FirebaseCallBack firebaseCallBack, int index){
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

        dbReferenceList.get(index).getReference().addListenerForSingleValueEvent(valueEventListener);
        dbReferenceList.get(index).setValueListener(valueEventListener);
    }

    private interface FirebaseCallBack {
        void onCallBack(List<String> customerDetails);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(int i=0; i < dbReferenceList.size(); i++)
            dbReferenceList.get(i).removeAllListener();
    }
}