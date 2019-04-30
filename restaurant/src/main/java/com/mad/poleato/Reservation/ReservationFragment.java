package com.mad.poleato.Reservation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HashMap<String,List<Dish>> listHash = new HashMap<>();
    private Display display;
    private Point size;
    private int width;
    String loggedID;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        initData();

        lv = view.findViewById(R.id.reservationslv);
        /**fix expandablelistview arrow position */
        lv.setIndicatorBounds(width-GetDipsFromPixel(35), width-GetDipsFromPixel(5));
        listAdapter = new ReservationExpandableListAdapter(getActivity(), reservations, listHash);
        lv.setAdapter(listAdapter);
        lv.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                Button b = (Button) view.findViewById(R.id.myButton);
                if(!lv.isGroupExpanded(i)){
                    b.setVisibility(View.VISIBLE);
                }else
                    b.setVisibility(View.GONE);
                return false;
            }
        });
        return view;
    }

    public int GetDipsFromPixel(float pixels){
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    private void initData(){
        reservations = new ArrayList<>();
        listHash = new HashMap<>();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("Reservation");

            reference.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d("Valerio", dataSnapshot.getKey());
                    //retrieve the customer (reservation) details
                    Reservation r;
                    String id, name, surname, date, time, address, status;
                    DataSnapshot detailsOfReservation= dataSnapshot.child("Details");

                    id= dataSnapshot.getKey();
                    name= detailsOfReservation.child("Name").getValue().toString();
                    surname= detailsOfReservation.child("Surname").getValue().toString();
                    date= detailsOfReservation.child("Date").getValue().toString();
                    time= detailsOfReservation.child("Time").getValue().toString();
                    address= detailsOfReservation.child("Address").getValue().toString();
                    status= dataSnapshot.child("Status").getValue().toString();
                    r= new Reservation(id, name, surname, address, date, time, status, getContext());
                    reservations.add(r);

                    //and for each customer (reservation) retrieve the list of dishes
                    DataSnapshot dishesOfReservation= dataSnapshot.child("Dish");
                    String nameDish, note;
                    Integer quantity;
                    Dish d;

                    for(DataSnapshot dish: dishesOfReservation.getChildren()) {
                        nameDish= dish.child("Name").getValue().toString();
                        quantity= Integer.parseInt(dish.child("Quantity").getValue().toString());
                        note= dish.child("Note").getValue().toString();
                        d= new Dish(nameDish, quantity, note);
                        r.addDishtoReservation(d);
                    }
                    listHash.put(r.getOrder_id(), r.getDishes());
                    listAdapter.notifyDataSetChanged();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d("Valerio", dataSnapshot.getKey());
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("Valerio", dataSnapshot.getKey());
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

            /*
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d("Valerio", "OnDataChange method");
                    // it is setted to the first record (restaurant)
                    // when the sign in and log in procedures will be handled, it will be the proper one
                    if (dataSnapshot.exists()) {
                        DataSnapshot issue = dataSnapshot.child(loggedID).child("Reservation");
                        // dataSnapshot is the "issue" node with all children
                        Reservation r;
                        String id, name, surname, date, time, address, status;
                        Long lenght= issue.getChildrenCount();
                        Log.d("Valerio", issue.getKey() + " #childs: " + lenght.toString());

                        //for each reservation in the table of logged restaurant
                        for(DataSnapshot snap : issue.getChildren()){
                            //retrieve the customer (reservation) details
                            lenght= snap.getChildrenCount();
                            Log.d("Valerio", issue.getKey() + " ->" + snap.getKey() + " #childs: " + lenght.toString());
                            DataSnapshot detailsOfReservation= snap.child("Details");
                            id= snap.getKey();
                            name= detailsOfReservation.child("Name").getValue().toString();
                            surname= detailsOfReservation.child("Surname").getValue().toString();
                            date= detailsOfReservation.child("Date").getValue().toString();
                            time= detailsOfReservation.child("Time").getValue().toString();
                            address= detailsOfReservation.child("Address").getValue().toString();
                            status= snap.child("Status").getValue().toString();
                            r= new Reservation(id, name, surname, address, date, time, status, getContext());
                            reservations.add(r);
                            //and for each customer (reservation) retrieve the list of dishes
                            DataSnapshot dishesOfReservation= snap.child("Dish");

                            lenght= dishesOfReservation.getChildrenCount();
                            Log.d("Valerio", issue.getKey() + " ->" + snap.getKey() + " ->" + dishesOfReservation.getKey() + " #childs: " + lenght.toString());
                            String nameDish, note;
                            Integer quantity;
                            Dish d;
                            for(DataSnapshot dish: dishesOfReservation.getChildren()) {
                                lenght= dish.getChildrenCount();
                                Log.d("Valerio", issue.getKey() + " ->" + snap.getKey() + " ->" + dishesOfReservation.getKey() + " ->" + dish.getKey() + " #childs: " + lenght.toString());
                                nameDish= dish.child("Name").getValue().toString();
                                quantity= Integer.parseInt(dish.child("Quantity").getValue().toString());
                                note= dish.child("Note").getValue().toString();
                                d= new Dish(nameDish, quantity, note);
                                r.addDishtoReservation(d);
                            }
                            listHash.put(r.getOrder_id(), r.getDishes());
                        }//for end
                    }
                    listAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                            " | MESSAGE: " + databaseError.getMessage());
                    Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
                }
            });
            */
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> statusPersistence= new ArrayList<>();
        ArrayList<String> textButtonPersistence= new ArrayList<>();

        for(int i=0; i<listAdapter.getGroupCount(); i++){
            View v= listAdapter.getGroupView(i, false, null, lv);
            TextView status= v.findViewById(R.id.tvStatusField);
            Button button= v.findViewById(R.id.myButton);
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
        if(savedInstanceState!=null) {
            statusPersistence = savedInstanceState.getStringArrayList("Status_Persistence");
            buttonTextPersistence= savedInstanceState.getStringArrayList("Button_Text_Persistence");
            if (statusPersistence != null && buttonTextPersistence != null)
                if (statusPersistence.size() > 0 && buttonTextPersistence.size()>0) {
                    for (int i = 0; i < listAdapter.getGroupCount(); i++) {
                        reservations.get(i).setStat(statusPersistence.get(i), getContext());
                        reservations.get(i).setButtonText(buttonTextPersistence.get(i));
                    }
                }
        }
    }

}
