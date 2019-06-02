package com.mad.poleato.OrderManagement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ExpandableListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.Classes.Food;

import com.mad.poleato.Interface;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * This fragment is about the menu of the selected restaurant
 */
public class MenuFragment extends Fragment {

    private Activity hostActivity;
    private ExpandableListView expListView;
    private MenuExpandableListAdapter listAdapter;
    private List<String> listDataGroup;
    private HashMap<String, List<Food>>listDataChild;
    int width;
    private int lastExpandedPosition = -1;
    private String restaurantID;
    private Order order;
    private Interface listener;

    private SortMenu sortMenu;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    double popularityAverage; // average of popularity counter of all foods in menu

    private enum groupType{
        STARTERS,
        FITSTS,
        SECONDS,
        DESSERTS,
        DRINKS
    }
    groupType currState;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();
        try {
            listener = (Interface) context;
            //get the order using interface from parent Activity
            order = listener.getOrder();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        //to sort the categories
        sortMenu = new SortMenu();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserID = currentUser.getUid();

        // OneSignal is used to send notifications between applications

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new HashMap<>();
        popularityAverage= 0;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View fragView = inflater.inflate(R.layout.menu_fragment_layout, container, false);

        //get the listview
        expListView = (ExpandableListView) fragView.findViewById(R.id.menuList);
        //fix position of ExpandableListView indicator.
        expListView.setIndicatorBounds(width-GetDipsFromPixel(35), width-GetDipsFromPixel(5));

        restaurantID = getArguments().getString("id");

        return fragView;
    }

    public int GetDipsFromPixel(float pixels){
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
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

        setList();
        // list Adapter of ExpandableList
        listAdapter = new MenuExpandableListAdapter(hostActivity, listDataGroup, listDataChild, order, listener);

        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("lastExpandedPosition", this.lastExpandedPosition);
        outState.putSerializable("listDataChild", this.listDataChild);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            this.lastExpandedPosition = savedInstanceState.getInt("lastExpandedPosition", -1);
            this.listDataChild = (HashMap<String, List<Food>>) savedInstanceState.getSerializable("listDataChild");

            for(String s : listDataChild.keySet()){
                for(Food f : listDataChild.get(s)){
                    this.listAdapter.insertChild(s, f);
                }

            }
        }
    }

    //Refresh listAdapter when fragment become visible
    //Needed for quantity changes between MenuFragment and FavoriteMenuFragment
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser)
            if(listAdapter!=null)
                listAdapter.notifyDataSetChanged();
    }

    /**
     * Interface is used also to keep track of changes to the order between
     * OrderActivity and CartActivity
     */
    @Override
    public void onResume() {
        super.onResume();
        try {
            //Using interface to get the order from parent activity
            order = listener.getOrder();
            listAdapter.setOrder(order);
            listAdapter.updateLitDataChild();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
        listAdapter.notifyDataSetChanged();
        expListView.setAdapter(listAdapter);
    }

    /**
     * Method called to set the ExpandableList
     */
    public void setList(){
        listDataGroup = new ArrayList<>();
        listDataChild = new HashMap<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                                        .child(restaurantID)
                                        .child("Menu");
        dbReferenceList.put("menu", new MyDatabaseReference(reference));

        // compute the popularity average for all dishes
        dbReferenceList.get("menu").setSingleValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long sum = 0;
                long numberOfFood= dataSnapshot.getChildrenCount();

                if(numberOfFood>0) {
                    for (DataSnapshot foodReference : dataSnapshot.getChildren()) {
                        sum += Integer.parseInt(foodReference.child("PopularityCounter").getValue().toString());
                    }

                    popularityAverage = sum / numberOfFood;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // fill the menu grouped by category (Firsts, seconds, Desserts, Drinks, Popular)

        dbReferenceList.get("menu").setChildListener(new ChildEventListener() {
                     @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Quantity") &&
                        dataSnapshot.hasChild("Description") &&
                        dataSnapshot.hasChild("Price") &&
                        dataSnapshot.hasChild("Category") &&
                        dataSnapshot.hasChild("photoUrl") &&
                        dataSnapshot.hasChild("PopularityCounter"))
                {
                    //set default image initially, then change if download is successful
                    final String id = dataSnapshot.getKey();
                    String name = dataSnapshot.child("Name").getValue().toString();
                    int quantity = Integer.parseInt(dataSnapshot.child("Quantity").getValue().toString());
                    String description = dataSnapshot.child("Description").getValue().toString();
                    int popularityCounter= Integer.parseInt(dataSnapshot.child("PopularityCounter").getValue().toString());

                    double price = Double.parseDouble(dataSnapshot.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    final String category = dataSnapshot.child("Category").getValue().toString();
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();

                    Food f = new Food(id, imageUrl, name, description, price, quantity);

                    if(!listDataGroup.contains(category))
                        listDataGroup.add(category);

                    if(!listDataChild.containsKey(category)){
                        listDataChild.put(category, new ArrayList<Food>());
                    }

                    listDataChild.get(category).add(f);

                    if(listDataChild.containsKey(category) &&
                            listDataChild.get(category).size() > 0)
                        Collections.sort(listDataChild.get(category));

                    /*
                     * if it's  popular food (popularity counter >= popularity average),
                     * add it to popular list
                     */
                    if(popularityCounter > popularityAverage) {
                        if(!listDataChild.containsKey("Popular"))
                            listDataChild.put("Popular", new ArrayList<Food>());

                        if(!listDataGroup.contains("Popular"))
                            listDataGroup.add("Popular");

                        listDataChild.get("Popular").add(f);

                        if(listDataChild.containsKey("Popular") &&
                                listDataChild.get("Popular").size() > 0)
                            Collections.sort(listDataChild.get("Popular"));
                    }

                    /*
                     * download food image
                     */
                    final int curr_index = listDataChild.get(category).size() - 1;
                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024*1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            String s = imageUrl;
                            Log.d("matte", "onSuccess");
                            setImg(category, id, s);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String s = "";
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage()
                                    +"| restaurantID: "+id);
                            setImg(category, id, s);
                        }
                    });
                    //Sorting categoris
                    Collections.sort(listDataGroup,sortMenu);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Quantity") &&
                        dataSnapshot.hasChild("Description") &&
                        dataSnapshot.hasChild("Price") &&
                        dataSnapshot.hasChild("Category") &&
                        dataSnapshot.hasChild("photoUrl")){

                    final String id = dataSnapshot.getKey();
                    String name = dataSnapshot.child("Name").getValue().toString();
                    int quantity = Integer.parseInt(dataSnapshot.child("Quantity").getValue().toString());
                    String description = dataSnapshot.child("Description").getValue().toString();
                    int popularityCounter= Integer.parseInt(dataSnapshot.child("PopularityCounter").getValue().toString());
                    double price = Double.parseDouble(dataSnapshot.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    final String category = dataSnapshot.child("Category").getValue().toString();
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();

                    Food f = new Food(id, imageUrl, name, description, price, quantity);

                    if(!listDataGroup.contains(category))
                        listDataGroup.add(category);

                    if(!listDataChild.containsKey(category)){
                        listDataChild.put(category,new ArrayList<Food>());
                    }

                    /*
                     * if it's  popular food (popularity counter >= popularity average),
                     * add it to popular list
                     */
                    if(popularityCounter > popularityAverage) {
                        if(!listDataChild.containsKey("Popular"))
                            listDataChild.put("Popular", new ArrayList<Food>());

                        if(!listDataGroup.contains("Popular"))
                            listDataGroup.add("Popular");

                        listDataChild.get("Popular").add(f);

                        if(listDataChild.containsKey("Popular") &&
                                listDataChild.get("Popular").size() > 0)
                            Collections.sort(listDataChild.get("Popular"));
                    }

                    int lenght= listDataChild.get(category).size();
                    boolean found= false;
                    int i;
                    for(i=0; i < lenght; i++){
                        if(listDataChild.get(category).get(i).getFoodID().equals(id)) {
                            found = true;
                            break;
                        }
                    }

                    //Check if food is already in the collections
                    if(found){
                        listDataChild.get(category).set(i, f);
                    }
                    else
                        listDataChild.get(category).add(f);

                    final int curr_index = listDataChild.get(category).size()-1;

                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            String s = imageUrl;
                            Log.d("matte", "onSuccess");
                            setImg(category, id, s);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String s = "";
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage()
                                    +"| restaurantID: "+id);
                            setImg(category, id, s);
                        }
                    });

                    if(listDataChild.containsKey(category) &&
                            listDataChild.get(category).size() > 0)
                        Collections.sort(listDataChild.get(category));

                    Collections.sort(listDataGroup,sortMenu);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Category")){
                    String category = dataSnapshot.child("Category").getValue().toString();
                    String id = dataSnapshot.getKey();

                    int toDelete = -1;
                    for(int idx = 0; idx < listDataChild.get(category).size(); idx ++){
                        if(listDataChild.get(category).get(idx).getFoodID().equals(id)){
                            toDelete = idx;
                            break;
                        }
                    }
                    if(toDelete != -1)
                        listDataChild.get(category).remove(toDelete);
                    if(listDataChild.get(category).size()==0){
                        listDataChild.remove(category);
                        listDataGroup.remove(category);
                    }

                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void setImg(String category, String foodID, String img){
        if(listDataChild.containsKey(category) && listDataChild.get(category) !=null) {
            for (Food f : listDataChild.get(category)) {
                if (f.getFoodID().equals(foodID))
                    f.setImg(img);
            }
        }
    }

    /**
     * Comparator to sort categories
     */
    private class SortMenu implements Comparator<String>{
        @Override
        public int compare(String s, String t1) {
            if(s.equals("Starters"))
                return -1;
            else if(t1.equals("Starters"))
                return 1;
            else if(s.equals("Popular"))
                return 1;
            else if(t1.equals("Popular"))
                return -1;
            else if(s.equals("Firsts") && (t1.equals("Seconds") || t1.equals("Desserts") || t1.equals("Drinks")))
                return -1;
            else if(s.equals("Seconds") && (t1.equals("Desserts") || t1.equals("Drinks")))
                return -1;
            else if(s.equals("Desserts") && t1.equals("Drinks"))
                return -1;
            else if(s.equals("Drinks"))
                return 1;
            else
                return 0;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();

        HashMap<String, MyDatabaseReference> referencesOfAdapter= listAdapter.getReferences();
        if(referencesOfAdapter != null)
            for (MyDatabaseReference my_ref : referencesOfAdapter.values())
                my_ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();

        HashMap<String, MyDatabaseReference> referencesOfAdapter= listAdapter.getReferences();
        if(referencesOfAdapter != null)
            for (MyDatabaseReference my_ref : referencesOfAdapter.values())
                my_ref.removeAllListener();
    }
}
