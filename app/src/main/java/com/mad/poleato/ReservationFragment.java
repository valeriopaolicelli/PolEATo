package com.mad.poleato;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//TODO status persistency

public class ReservationFragment extends Fragment {
    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Reservation> reservations;
    private HashMap<String,List<Dish>> listHash = new HashMap<>();
    private Display display;
    private Point size;
    private int width;
    private Reservation c1;
    private Reservation c2;
    private Reservation c3;

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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        initData();


        lv = view.findViewById(R.id.reservationslv);

        //fix expandablelistview arrow position
        lv.setIndicatorBounds(width-GetDipsFromPixel(35), width-GetDipsFromPixel(5));

        listAdapter = new ReservationExpandableListAdapter(getActivity(),reservations,listHash);

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
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    private void initData(){

        reservations = new ArrayList<>();
        listHash = new HashMap<>();

        c1 = new Reservation("A100","Fabio", "Ricciardi", "Corso duca degli abruzzi, 24","04/04/2019", "20.30", getContext());
        c2 = new Reservation("A101","Michelangelo", "Moncada", "Via degli esempi, 404", "04/04/2019", "19.20", getContext());
        c3 = new Reservation("A102","Valerio", "Paolicelli", "Via delle prove, 101", "04/04/2019", "21.20", getContext());

        reservations.add(c1);
        reservations.add(c2);
        reservations.add(c3);

        Dish d1_c1= new Dish("Pasta carbonara", 1, "no cipolla");
        Dish d2_c1= new Dish("Pizza margherita", 2, "pizze tagliate");

        c1.addDishtoReservation(d1_c1);
        c1.addDishtoReservation(d2_c1);

        Dish d1_c2= new Dish("Pasta carbonara", 1, "no cipolla");
        c2.addDishtoReservation(d1_c2);

        Dish d1_c3= new Dish("Pasta amatriciana", 1, "no formaggio, tanto guanciale");
        Dish d2_c3= new Dish("Pizza margherita", 1, "pizze tagliate");
        c3.addDishtoReservation(d1_c3);
        c3.addDishtoReservation(d2_c3);

        listHash.put(c1.getOrder_id(),c1.getDishes());
        listHash.put(c2.getOrder_id(),c2.getDishes());
        listHash.put(c3.getOrder_id(),c3.getDishes());
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