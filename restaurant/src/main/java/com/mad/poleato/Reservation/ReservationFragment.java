package com.mad.poleato.Reservation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.graphics.Point;
import android.view.Display;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.mad.poleato.R;
import com.mad.poleato.Reservation.ReservationListManagement.ReservationExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Reservation c1;
    private Reservation c2;
    private Reservation c3;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        initData();


        lv = view.findViewById(R.id.reservationslv);

        /**fix expandablelistview arrow position */
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
        /** Get the screen's density scale*/
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
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
