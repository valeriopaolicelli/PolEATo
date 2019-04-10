package com.example.poleato;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.poleato.ExpandableListManagement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DailyOfferFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataGroup;
    private HashMap<String, List<Food>> listDataChild;
    private FloatingActionButton floatingActionButton;
    private Display display;
    private Point size;
    int width;
    private int lastExpandedPosition = -1;
    private FragmentDListener fragmentDListener;


    public interface  FragmentDListener {
        void onInputDSent(Object o);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(context instanceof FragmentDListener){
            fragmentDListener = (FragmentDListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement FragmentDListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Calculate position of ExpandableListView indicator.
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;


        /* TODO HERE RESUME THE SAVED DATA FROM SHARED PREFERENCES */

        // preparing list data
        prepareListData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.dailyoffer_frag_layout, container, false);
        // get the listview
        expListView = (ExpandableListView) fragView.findViewById(R.id.menuList);
        // fix position of ExpandableListView indicator.
        expListView.setIndicatorBounds(width-180,0);

        floatingActionButton = fragView.findViewById(R.id.floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentDListener.onInputDSent(this);
            }
        });

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // to collapse all groups except the one tapped
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

        listAdapter = new ExpandableListAdapter(hostActivity, listDataGroup, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // attach the onClickListener to the cardView to launch a fragment to edit the infos
//        CardView cv = hostActivity.findViewById(R.id.childView);
//        cv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////
////                EditFoodFragment nextFrag= new EditFoodFragment();
////                FragmentTransaction transaction = getFragmentManager().beginTransaction();
////                transaction.replace(R.id.menuList, nextFrag); // give your fragment container id in first parameter
////                transaction.commit();
//            }
//        });

    }

    /*
     * Preparing the data list
     */
    private void prepareListData() {
        listDataGroup = new ArrayList<String>();
        listDataChild = new HashMap<String, List<Food>>();

        // Adding child data
        listDataGroup.add(getString(R.string.starters));
        listDataGroup.add(getString(R.string.firsts));
        listDataGroup.add(getString(R.string.seconds));
        listDataGroup.add(getString(R.string.desserts));
        listDataGroup.add(getString(R.string.drinks));

        // Adding child data
        List<Food> starters = new ArrayList<Food>();
        starters.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.caprese),
                "Caprese", "Pomodori, mozzarella, olio e basilico", 2.50, 10));
        starters.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.bruschette),
                "Bruschette", "Pane, pomodori, olio e basilico", 1.80, 10));


        List<Food> firsts = new ArrayList<Food>();
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.carbonara),
                "Carbonara", "Spaghetti, guanciale, uovo, pepe e pecorino", 5.00,10));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.amatriciana),
                "Amatriciana", "Pasta, pancetta, pomodoro, peperoncino", 3.50,10));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.lasagna),
                "Lasagna", "Pomodoro, formaggio e basilico", 6.00, 5));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.gamberetti),
                "Gamberetti", "Pomodoro, gamberetti e melanzane", 7.00, 7));

        List<Food> seconds = new ArrayList<Food>();
        seconds.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.pollo),
                "Pollo al forno", "Pollo, patate e pomodoro", 8.00, 10));

        List<Food> desserts = new ArrayList<Food>();
        desserts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.tiramisu),
                "Tiramisu", "Caffè, savoiardi, mascarpone e cacao", 2.00, 10));

        List<Food> drinks = new ArrayList<Food>();
        drinks.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.tiramisu),
                "Poretti 33cl", "Birra", 2.00, 10));


        listDataChild.put(listDataGroup.get(0), starters); // Header, Child data
        listDataChild.put(listDataGroup.get(1), firsts);
        listDataChild.put(listDataGroup.get(2), seconds);
        listDataChild.put(listDataGroup.get(3), desserts);
        listDataChild.put(listDataGroup.get(4), drinks);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentDListener = null;
    }
}

