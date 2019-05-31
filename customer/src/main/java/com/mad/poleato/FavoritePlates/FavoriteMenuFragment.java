package com.mad.poleato.FavoritePlates;

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
import com.mad.poleato.OrderManagement.MenuFragment;
import com.mad.poleato.OrderManagement.Order;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Fragment that shows the favorite dishes of the selected restaurant
 */
public class FavoriteMenuFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;
    private ExpandableListView expListView;
    private FavoriteMenuExpandableListAdapter listAdapter;
    private List<String> listDataGroup;
    private HashMap<String, List<Food>>listDataChild;
    private Display display;
    private Point size;
    int width;
    private int lastExpandedPosition = -1;
    private String restaurantID;
    private Order order;
    private Interface listener;

    private String currentUserID;
    private FirebaseAuth mAuth;

    private SortMenu sortMenu;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private HashMap<String, MyDatabaseReference> dbReferenceList;


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
            order = listener.getOrder();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

        //to sort the categories
        sortMenu = new SortMenu();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

// OneSignal is used to send notifications between applications

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragView = inflater.inflate(R.layout.favorite_plate_menu_fragment_layout,container,false );

        //get the listview
        expListView = (ExpandableListView) fragView.findViewById(R.id.menuList);
        //fix position of ExpandableListView indicator.
        expListView.setIndicatorBounds(width-GetDipsFromPixel(35), width-GetDipsFromPixel(5));

        restaurantID = getArguments().getString("id");
        //restaurantID= "8toBMYgZThTy9eySxX7NdNSxQTF3";

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
        listAdapter = new FavoriteMenuExpandableListAdapter(hostActivity, listDataGroup, listDataChild, order, listener);

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

    @Override
    public void onResume() {
        super.onResume();
        try {
            order = listener.getOrder();
            listAdapter.setOrder(order);
            listAdapter.updateLitDataChild();
        } catch (ClassCastException castException) {
            /** The activity does not implement the listener. */
        }
        listAdapter.notifyDataSetChanged();
        expListView.setAdapter(listAdapter);
    }



    public void setList(){
        listDataGroup = new ArrayList<>();
        listDataChild = new HashMap<>();

        DatabaseReference referenceFavoriteCustomer = FirebaseDatabase.getInstance()
                                                            .getReference("customers/" + currentUserID +
                                                                                "/Favorite/" + restaurantID +
                                                                                "/dishes");
        dbReferenceList.put("favorite", new MyDatabaseReference(referenceFavoriteCustomer));

        dbReferenceList.get("favorite").setChildListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot currentFavoriteDataSnapshot, @Nullable String s) {
                        final String favoriteFoodID = currentFavoriteDataSnapshot.getKey();
                        DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance()
                                .getReference("restaurants/" + restaurantID +
                                        "/Menu");
                        dbReferenceList.put("menu", new MyDatabaseReference(referenceRestaurant));

                        dbReferenceList.get("menu").setChildListener(new ChildEventListener() {

                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshotRest, @Nullable String s) {
                                        if (dataSnapshotRest.getKey().equals(favoriteFoodID)) {
                                            if (dataSnapshotRest.hasChild("Name") &&
                                                    dataSnapshotRest.hasChild("Quantity") &&
                                                    dataSnapshotRest.hasChild("Description") &&
                                                    dataSnapshotRest.hasChild("Price") &&
                                                    dataSnapshotRest.hasChild("Category") &&
                                                    dataSnapshotRest.hasChild("photoUrl")) {
                                                //set default image initially, then change if download is successful
                                                String name = dataSnapshotRest.child("Name").getValue().toString();
                                                int quantity = Integer.parseInt(dataSnapshotRest.child("Quantity").getValue().toString());
                                                String description = dataSnapshotRest.child("Description").getValue().toString();
                                                double price = Double.parseDouble(dataSnapshotRest.child("Price")
                                                        .getValue()
                                                        .toString()
                                                        .replace(",", "."));
                                                final String category = dataSnapshotRest.child("Category").getValue().toString();
                                                final String imageUrl = dataSnapshotRest.child("photoUrl").getValue().toString();

                                                Food f = new Food(favoriteFoodID, imageUrl, name, description, price, quantity);

                                                if (!listDataGroup.contains(category))
                                                    listDataGroup.add(category);
                                                if (!listDataChild.containsKey(category)) {
                                                    listDataChild.put(category, new ArrayList<Food>());
                                                }
                                                listDataChild.get(category).add(f);


                                                /*
                                                 * download food image
                                                 */
                                                final int curr_index = listDataChild.get(category).size() - 1;
                                                StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                final long ONE_MEGABYTE = 1024 * 1024;
                                                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        String s = imageUrl;
                                                        Log.d("matte", "onSuccess");
                                                        //SerialBitmap bmp = new SerialBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                        setImg(category, curr_index, s);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        String s = "";
                                                        Log.d("matte", "onFailure() : excp -> " + exception.getMessage()
                                                                + "| restaurantID: " + favoriteFoodID);
                                                        setImg(category, curr_index, s);
                                                    }
                                                });

                                                Collections.sort(listDataGroup,sortMenu);
                                                listAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot dataSnapshotRest, @Nullable String s) {
                                        if (dataSnapshotRest.getKey().equals(favoriteFoodID)) {
                                            if (dataSnapshotRest.hasChild("Name") &&
                                                    dataSnapshotRest.hasChild("Quantity") &&
                                                    dataSnapshotRest.hasChild("Description") &&
                                                    dataSnapshotRest.hasChild("Price") &&
                                                    dataSnapshotRest.hasChild("Category") &&
                                                    dataSnapshotRest.hasChild("photoUrl")) {

                                                String name = dataSnapshotRest.child("Name").getValue().toString();
                                                int quantity = Integer.parseInt(dataSnapshotRest.child("Quantity").getValue().toString());
                                                String description = dataSnapshotRest.child("Description").getValue().toString();
                                                double price = Double.parseDouble(dataSnapshotRest.child("Price")
                                                        .getValue()
                                                        .toString()
                                                        .replace(",", "."));
                                                final String category = dataSnapshotRest.child("Category").getValue().toString();
                                                final String imageUrl = dataSnapshotRest.child("photoUrl").getValue().toString();

                                                Food f = new Food(favoriteFoodID, imageUrl, name, description, price, quantity);

                                                if (!listDataGroup.contains(category))
                                                    listDataGroup.add(category);
                                                if (!listDataChild.containsKey(category)) {
                                                    listDataChild.put(category, new ArrayList<Food>());
                                                }

                                                int lenght = listDataChild.get(category).size();
                                                boolean found = false;
                                                int i;
                                                for (i = 0; i < lenght; i++) {
                                                    if (listDataChild.get(category).get(i).getFoodID().equals(favoriteFoodID)) {
                                                        found = true;
                                                        break;
                                                    }
                                                }

                                                if (found) {
                                                    listDataChild.get(category).set(i, f);
                                                } else
                                                    listDataChild.get(category).add(f);

                                                final int curr_index = listDataChild.get(category).size() - 1;

                                                StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                                                final long ONE_MEGABYTE = 1024 * 1024;
                                                photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        String s = imageUrl;
                                                        Log.d("matte", "onSuccess");
                                                        // SerialBitmap bmp = new SerialBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                                                        setImg(category, curr_index, s);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception exception) {
                                                        String s = "";
                                                        Log.d("matte", "onFailure() : excp -> " + exception.getMessage()
                                                                + "| restaurantID: " + favoriteFoodID);
                                                        setImg(category, curr_index, s);
                                                    }
                                                });

                                                Collections.sort(listDataGroup,sortMenu);
                                                listAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshotRest) {
                                        if (dataSnapshotRest.getKey().equals(favoriteFoodID)) {
                                            if (dataSnapshotRest.hasChild("Category")) {
                                                String category = dataSnapshotRest.child("Category").getValue().toString();

                                                int toDelete = -1;
                                                for (int idx = 0; idx < listDataChild.get(category).size(); idx++) {
                                                    if (listDataChild.get(category).get(idx).getFoodID().equals(favoriteFoodID)) {
                                                        toDelete = idx;
                                                        break;
                                                    }
                                                }
                                                if (toDelete != -1)
                                                    listDataChild.get(category).remove(toDelete);
                                                if (listDataChild.get(category).size() == 0) {
                                                    listDataChild.remove(category);
                                                    listDataGroup.remove(category);
                                                }

                                                Collections.sort(listDataGroup,sortMenu);
                                                listAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot dataSnapshotRest, @Nullable String s) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                }); // end of child listener of restaurant menu
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot currentFavoriteDataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot currentFavoriteDataSnapshot) {
                        final String favoriteFoodID = currentFavoriteDataSnapshot.getKey();
                        DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance()
                                .getReference("restaurants/" + restaurantID +
                                        "/Menu");
                        dbReferenceList.put("menuRemove", new MyDatabaseReference(referenceRestaurant));

                        dbReferenceList.get("menuRemove").setValueListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshotRest) {
                                        for(DataSnapshot plateInMenuDataSnapshot : dataSnapshotRest.getChildren()) {
                                            if (plateInMenuDataSnapshot.getKey().equals(favoriteFoodID)) {
                                                if (plateInMenuDataSnapshot.hasChild("Category")) {
                                                    String category = plateInMenuDataSnapshot.child("Category").getValue().toString();

                                                    int toDelete = -1;
                                                    for (int idx = 0; idx < listDataChild.get(category).size(); idx++) {
                                                        if (listDataChild.get(category).get(idx).getFoodID().equals(favoriteFoodID)) {
                                                            toDelete = idx;
                                                            break;
                                                        }
                                                    }
                                                    if (toDelete != -1)
                                                        listDataChild.get(category).remove(toDelete);
                                                    if (listDataChild.get(category).size() == 0) {
                                                        listDataChild.remove(category);
                                                        listDataGroup.remove(category);
                                                    }

                                                    Collections.sort(listDataGroup,sortMenu);
                                                    listAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot currentFavoriteDataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }

        });
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

    public void setImg(String category, int idx, String img){
        listDataChild.get(category).get(idx).setImg(img);
    }

    private class SortMenu implements Comparator<String>{
        @Override
        public int compare(String s, String t1) {
            if(s.equals("Starters"))
                return -1;
            else if(t1.equals("Starters"))
                return 1;
            else if(s.equals("Drinks"))
                return 1;
            else if(t1.equals("Drinks"))
                return -1;
            else if(s.equals("Firsts") && (t1.equals("Seconds") || t1.equals("Desserts")))
                return -1;
            else if(s.equals("Seconds") && (t1.equals("Desserts")))
                return -1;
            else if(s.equals("Desserts"))
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }
}
