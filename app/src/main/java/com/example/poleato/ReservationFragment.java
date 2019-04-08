package com.example.poleato;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO status persistency
//TODO add name, surname, address in the box, when the order is on delivering

public class ReservationFragment extends Fragment {
    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<Reservation> reservations;
    private HashMap<String,List<Dish>> listHash = new HashMap<>();
    private Display display;
    private Point size;
    private int width;
    private Reservation c1;
    private Reservation c2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Calculate position of ExpandableListView indicator.
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        initData();


        lv = view.findViewById(R.id.reservationslv);

        //fix expandablelistview arrow position
        lv.setIndicatorBounds(width-160, 0);

        listAdapter = new ReservationExpandableListAdapter(getActivity(),reservations,listHash);

        lv.setAdapter(listAdapter);

        return view;
    }

    private void initData(){

        reservations = new ArrayList<>();
        listHash = new HashMap<>();

        c1 = new Reservation("ID111111","Fabio", "Ricciardi", "04/04/2019", "20.30", getContext());
        c2 = new Reservation("ID222222","Michelangelo", "Moncada", "04/04/2019", "19.20", getContext());

        reservations.add(c1);
        reservations.add(c2);


        Dish d1_c1= new Dish("Pasta carbonara", 1, "no cipolla");
        Dish d2_c1= new Dish("Pizza margherita", 2, "pizze tagliate");
        List<Dish> list1= new ArrayList<>();
        list1.add(d1_c1);
        list1.add(d2_c1);
        List<Dish> list2 = new ArrayList<>();
        list2.add(d1_c1);

        listHash.put(c1.getOrder_id(),list1);
        listHash.put(c2.getOrder_id(),list2);
    }
}