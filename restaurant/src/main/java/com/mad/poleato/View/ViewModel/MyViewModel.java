package com.mad.poleato.View.ViewModel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.mad.poleato.R;
import com.mad.poleato.DailyOffer.Food;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MyViewModel extends ViewModel {
    private MutableLiveData<List<String>> _listDataGroup = new MutableLiveData<>(); // header titles
    private final MutableLiveData<TreeMap<String, List<Food>>> _listDataChild = new MutableLiveData(); // child data in format of header title, child title


    public LiveData<TreeMap<String, List<Food>>> getListC(){
        return _listDataChild;
    }

    public LiveData<List<String>> getListG(){
        return _listDataGroup;
    }

    public void setData (List<String> listDataGroup, TreeMap<String, List<Food>> listDataChild) {
        _listDataGroup.setValue(listDataGroup);
        _listDataChild.postValue(listDataChild);
    }


    public void prepareListData(Context context) {

        List<String> listDataGroup = new ArrayList<String>();
        TreeMap<String, List<Food>> listDataChild = new TreeMap<String, List<Food>>();

        /** Adding child data*/
        listDataGroup.add(context.getString(R.string.starters));
        listDataGroup.add(context.getString(R.string.firsts));
        listDataGroup.add(context.getString(R.string.seconds));
        listDataGroup.add(context.getString(R.string.desserts));
        listDataGroup.add(context.getString(R.string.drinks));

        /** Adding child data */
        List<Food> starters = new ArrayList<Food>();
        starters.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.caprese),
                "Caprese", "Pomodori, mozzarella, olio e basilico", 2.50, 10));
        starters.add(new Food(BitmapFactory.decodeResource(Resources.getSystem(), R.drawable.bruschette),
                "Bruschette", "Pane, pomodori, olio e basilico", 1.80, 10));


        List<Food> firsts = new ArrayList<Food>();
        firsts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.carbonara),
                "Carbonara", "Spaghetti, guanciale, uovo, pepe e pecorino", 5.00,10));
        firsts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.amatriciana),
                "Amatriciana", "Pasta, pancetta, pomodoro, peperoncino", 3.50,10));
        firsts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.lasagna),
                "Lasagna", "Pomodoro, formaggio e basilico", 6.00, 5));
        firsts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.gamberetti),
                "Gamberetti", "Pomodoro, gamberetti e melanzane", 7.00, 7));

        List<Food> seconds = new ArrayList<Food>();
        seconds.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.pollo),
                "Pollo al forno", "Pollo, patate e pomodoro", 8.00, 10));

        List<Food> desserts = new ArrayList<Food>();
        desserts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.tiramisu),
                "Tiramisu", "Caffè, savoiardi, mascarpone e cacao", 2.00, 10));

        List<Food> drinks = new ArrayList<Food>();
        drinks.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.poretti),
                "Poretti 33cl", "Birra", 2.00, 10));


        listDataChild.put(listDataGroup.get(0), starters); // Header, Child data
        listDataChild.put(listDataGroup.get(1), firsts);
        listDataChild.put(listDataGroup.get(2), seconds);
        listDataChild.put(listDataGroup.get(3), desserts);
        listDataChild.put(listDataGroup.get(4), drinks);


        setData (listDataGroup, listDataChild);

    }

}
