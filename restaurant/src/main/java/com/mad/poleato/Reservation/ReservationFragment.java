package com.mad.poleato.Reservation;

import android.app.ProgressDialog;
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

import com.mad.poleato.NavigatorActivity;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;
import com.mad.poleato.SignInActivity;
import com.mad.poleato.SignUpActivity;

import java.util.ArrayList;
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
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
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

                Navigation.findNavController(view).navigate(R.id.action_reservation_id_to_signInActivity);
                getActivity().finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
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
                    //retrieve the customer (reservation) details

                    order_id = dataSnapshot.getKey();
                    customer_id = dataSnapshot.child("customerID").getValue().toString();
                    final String date= dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();


                    //TODO update with proper date, time and notes

                    //Retrieve through customerID the details of the customer
                    customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);

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

                    });


                    // fields setted to null only because they will be setted later in the call back of FB
                    r = new Reservation(order_id, null, null, null, date, time,
                            status, null, totalPrice, getActivity());
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

                    listAdapter.notifyDataSetChanged();
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
                    final String date= dataSnapshot.child("date").getValue().toString();
                    final String time= dataSnapshot.child("time").getValue().toString();
                    final String status = dataSnapshot.child("status").child(localeShort).getValue().toString();
                    final String totalPrice= dataSnapshot.child("totalPrice").getValue().toString();
                    String note= null;
                    ArrayList<Dish> dishes= new ArrayList<>();

                    //TODO update with proper date, time and notes

                    //Retrieve through customerID the details of the customer
                    customer= FirebaseDatabase.getInstance().getReference("customers").child(customer_id);
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
                        d = new Dish(nameDish, quantity, note, foodID);

                        dishes.add(d);
                    }

                    Reservation r = new Reservation(order_id, null, null, null, date, time,
                            status, null, totalPrice, getContext());

                    // if the status is changed (onclick listener) the order must change only and not re-added
                    if(!listHash.containsKey(order_id)){
                        reservations.add(r);
                    }
                    listHash.put(order_id, dishes);

                    for(Reservation res : reservations)
                            if(res.getOrder_id().equals(order_id))
                                res.setStat(status);

                    listAdapter.notifyDataSetChanged();

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

    private void readData(final FirebaseCallBack firebaseCallBack){
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

        customer.addListenerForSingleValueEvent(valueEventListener);
    }

    private interface FirebaseCallBack {
        void onCallBack(List<String> customerDetails);
    }
}