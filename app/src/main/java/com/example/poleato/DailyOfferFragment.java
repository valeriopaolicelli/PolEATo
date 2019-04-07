package com.example.poleato;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import com.example.poleato.ExpandableListManagement.*;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DailyOfferFragment extends Fragment {

    Activity hostActivity;
    View rootview;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataGroup;
    HashMap<String, List<Food>> listDataChild;
    private int lastExpandedPosition = -1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* TODO HERE RESUME THE SAVED DATA FROM SHARED PREFERENCES */

        // preparing list data
        prepareListData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootview = inflater.inflate(R.layout.menu, container, false);

        return rootview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get the listview
        expListView = (ExpandableListView) rootview.findViewById(R.id.menuList);

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
                "Caprese", "Pomodori, mozzarella, olio e basilico", 2.50));
        starters.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.bruschette),
                "Bruschette", "Pane, pomodori, olio e basilico", 1.80));


        List<Food> firsts = new ArrayList<Food>();
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.carbonara),
                "Carbonara", "Spaghetti, guanciale, uovo, pepe e pecorino", 5.00));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.amatriciana),
                "Amatriciana", "Pasta, pancetta, pomodoro, peperoncino", 3.50));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.lasagna),
                "Lasagna", "Pomodoro, formaggio e basilico", 6.00));
        firsts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.gamberetti),
                "Gamberetti", "Pomodoro, gamberetti e melanzane", 7.00));

        List<Food> seconds = new ArrayList<Food>();
        seconds.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.pollo),
                "Pollo al forno", "Pollo, patate e pomodoro", 8.00));

        List<Food> desserts = new ArrayList<Food>();
        desserts.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.tiramisu),
                "Tiramisu", "Caffè, savoiardi, mascarpone e cacao", 2.00));

        List<Food> drinks = new ArrayList<Food>();
        drinks.add(new Food(BitmapFactory.decodeResource(getResources(), R.drawable.tiramisu),
                "Poretti 33cl", "Birra", 2.00));


        listDataChild.put(listDataGroup.get(0), starters); // Header, Child data
        listDataChild.put(listDataGroup.get(1), firsts);
        listDataChild.put(listDataGroup.get(2), seconds);
        listDataChild.put(listDataGroup.get(3), desserts);
        listDataChild.put(listDataGroup.get(4), drinks);

    }

}

