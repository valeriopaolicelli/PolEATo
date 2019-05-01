package com.mad.poleato.Reservation;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.mad.poleato.R;
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;

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

    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>> listHash = new HashMap<>();
    private List<String> customerDetails;
    private Display display;
    private Point size;
    private int width;
    String loggedID;
    private String localeShort;

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
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        /** Calculate position of ExpandableListView indicator. */
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;
        loggedID = "R00";
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout, container, false);
        initData();

        lv = view.findViewById(R.id.reservationslv);
        /*fix expandablelistview arrow position */
        lv.setIndicatorBounds(width - GetDipsFromPixel(35), width - GetDipsFromPixel(5));
        listAdapter = new ReservationExpandableListAdapter(getActivity(), reservations, listHash);
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

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations");
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //retrieve the customer (reservation) details
                Reservation r= null;
                final String[] status = {null};
                final String order_id, customer_id;
                final DataSnapshot dataSnapshot1= dataSnapshot;
                String note= null;

                Locale locale= Locale.getDefault();
                localeShort = locale.toString().substring(0, 2);

                order_id = dataSnapshot.getKey();
                customer_id = dataSnapshot.child("customerID").getValue().toString();
                final String date= dataSnapshot.child("date").getValue().toString();
                final String time= dataSnapshot.child("time").getValue().toString();

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
//                                status[0] = dataSnapshot1.child("status").child(localeShort).getValue().toString();
  //                              r.setStat(status[0], getContext());
                                r.setDate(date);
                                r.setTime(time);
                            }
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                });

                // fields setted to null only because they will be setted later in the call back of FB
                r = new Reservation(order_id, null, null, null, null, null, null, null, getContext());
                reservations.add(r);

                //and for each customer (reservation) retrieve the list of dishes
                DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                String nameDish;
                int quantity;
                Dish d;

                for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                    nameDish = dish.child("name").getValue().toString();
                    quantity = Integer.parseInt(dish.child("selectedQuantity").getValue().toString());
                    note= dish.child("customerNotes").getValue().toString();
                    d = new Dish(nameDish, quantity, note);
                    r.addDishtoReservation(d);
                }
                listHash.put(r.getOrder_id(), r.getDishes());
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("Valerio", dataSnapshot.getKey());
                final DataSnapshot dataSnapshot1= dataSnapshot;
                final String[] status = {null};
                final String order_id= dataSnapshot.getKey();
                final String customer_id= dataSnapshot.child("customerID").getValue().toString();
                final String date= dataSnapshot.child("date").getValue().toString();
                final String time= dataSnapshot.child("time").getValue().toString();
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
      //                          status[0] = dataSnapshot1.child("status").child(localeShort).getValue().toString();
      //                          r.setStat(status[0], getContext());
                                r.setDate(date);
                                r.setTime(time);
                            }
                        }
                        listAdapter.notifyDataSetChanged();
                    }
                });

                //and for each customer (reservation) retrieve the list of dishes
                DataSnapshot dishesOfReservation = dataSnapshot.child("dishes");
                String nameDish;
                int quantity;
                Dish d;

                for (DataSnapshot dish : dishesOfReservation.getChildren()) {
                    nameDish = dish.child("name").getValue().toString();
                    quantity = Integer.parseInt(dish.child("selectedQuantity").getValue().toString());
                    d = new Dish(nameDish, quantity, note);
                    dishes.add(d);
                }

                listAdapter.notifyDataSetChanged();
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
                Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
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
                        reservations.get(i).setStat(statusPersistence.get(i), getContext());
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
                Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        };

        customer.addListenerForSingleValueEvent(valueEventListener);
    }

    private interface FirebaseCallBack {
        void onCallBack(List<String> customerDetails);
    }
}