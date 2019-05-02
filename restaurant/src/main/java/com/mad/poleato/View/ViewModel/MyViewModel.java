package com.mad.poleato.View.ViewModel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.R;
import com.mad.poleato.DailyOffer.Food;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MyViewModel extends ViewModel {
    private MutableLiveData<List<String>> _listDataGroup = new MutableLiveData<>(); // header titles
    private MutableLiveData<HashMap<String, List<Food>>> _listDataChild = new MutableLiveData<>(); // child data in format of header title, child title
    private boolean flag = false;

    private String loggedID = "R05";

    public LiveData<HashMap<String, List<Food>>> getListC() {
        return _listDataChild;
    }

    public LiveData<List<String>> getListG() {
        return _listDataGroup;
    }

    public void setData(List<String> listDataGroup, HashMap<String, List<Food>> listDataChild) {
        _listDataGroup.setValue(listDataGroup);
        _listDataChild.postValue(listDataChild);
    }


    public void insertChild(final int groupPosition, Food food) {
        this._listDataChild.getValue().get(getGroup(groupPosition).toString()).add(food);
    }

    public void insertChild(String groupTag, Food food) {
        this._listDataChild.getValue().get(groupTag).add(food);
    }

    public void removeChild(final int groupPosition, final int childPosition) {
        this._listDataChild.getValue().get(getGroup(groupPosition).toString()).remove(childPosition);
    }

    public Object getGroup(int groupPosition) {
        return this._listDataGroup.getValue().get(groupPosition);
    }


    private void initGroup(Context context){
        List<String> l = new ArrayList<>();
        l.add(context.getString(R.string.starters));
        l.add(context.getString(R.string.firsts));
        l.add(context.getString(R.string.seconds));
        l.add(context.getString(R.string.desserts));
        l.add(context.getString(R.string.drinks));

        _listDataGroup.setValue(l);
    }

    private void initChild() {

        _listDataChild.setValue(new HashMap<String, List<Food>>());
        for (String s : _listDataGroup.getValue())
            _listDataChild.getValue().put(s, new ArrayList<Food>());

    }


    public void downloadMenu(final Context context){

        initGroup(context);
        initChild();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants/"+loggedID+"/Menu");

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                List<Food> l = new ArrayList<>();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    String name = snap.getKey();
                    String ingredients = snap.child("Description").getValue().toString();
                    double price = Double.parseDouble(snap.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    int quantity = Integer.parseInt(snap.child("Quantity").getValue().toString());
                    //TODO make image dynamic
                    Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.plate_fork);
                    l.add(new Food(img, name, ingredients, price, quantity));
                }

                if(dataSnapshot.getKey().equals("Drinks"))
                    _listDataChild.getValue().put(context.getString(R.string.drinks), l);
                else if(dataSnapshot.getKey().equals("Firsts"))
                    _listDataChild.getValue().put(context.getString(R.string.firsts), l);
                else if(dataSnapshot.getKey().equals("Seconds"))
                    _listDataChild.getValue().put(context.getString(R.string.seconds), l);
                else if(dataSnapshot.getKey().equals("Starters"))
                    _listDataChild.getValue().put(context.getString(R.string.starters), l);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                List<Food> l = new ArrayList<>();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    String name = snap.getKey();
                    String ingredients = snap.child("Description").getValue().toString();
                    double price = Double.parseDouble(snap.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    int quantity = Integer.parseInt(snap.child("Quantity").getValue().toString());
                    //TODO make image dynamic
                    Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.plate_fork);
                    l.add(new Food(img, name, ingredients, price, quantity));
                }

                if(dataSnapshot.getKey().equals("Drinks"))
                    _listDataChild.getValue().put(context.getString(R.string.drinks), l);
                else if(dataSnapshot.getKey().equals("Firsts"))
                    _listDataChild.getValue().put(context.getString(R.string.firsts), l);
                else if(dataSnapshot.getKey().equals("Seconds"))
                    _listDataChild.getValue().put(context.getString(R.string.seconds), l);
                else if(dataSnapshot.getKey().equals("Starters"))
                    _listDataChild.getValue().put(context.getString(R.string.starters), l);

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                List<Food> l = new ArrayList<>();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    String name = snap.getKey();
                    String ingredients = snap.child("Description").getValue().toString();
                    double price = Double.parseDouble(snap.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    int quantity = Integer.parseInt(snap.child("Quantity").getValue().toString());
                    //TODO make image dynamic
                    Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.plate_fork);
                    l.add(new Food(img, name, ingredients, price, quantity));
                }

                if(dataSnapshot.getKey().equals("Drinks"))
                    _listDataChild.getValue().put(context.getString(R.string.drinks), l);
                else if(dataSnapshot.getKey().equals("Firsts"))
                    _listDataChild.getValue().put(context.getString(R.string.firsts), l);
                else if(dataSnapshot.getKey().equals("Seconds"))
                    _listDataChild.getValue().put(context.getString(R.string.seconds), l);
                else if(dataSnapshot.getKey().equals("Starters"))
                    _listDataChild.getValue().put(context.getString(R.string.starters), l);

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }

    public void prepareListData(Context context) {

        if (flag == false) {

            flag = true;
            List<String> listDataGroup = new ArrayList<String>();
            HashMap<String, List<Food>> listDataChild = new HashMap<String, List<Food>>();

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
                    "Carbonara", "Spaghetti, guanciale, uovo, pepe e pecorino", 5.00, 10));
            firsts.add(new Food(BitmapFactory.decodeResource(context.getResources(), R.drawable.amatriciana),
                    "Amatriciana", "Pasta, pancetta, pomodoro, peperoncino", 3.50, 10));
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


            setData(listDataGroup, listDataChild);
        }
    }

}
