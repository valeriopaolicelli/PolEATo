package com.mad.poleato;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MenuFragment extends Fragment {

    private Activity hostActivity;
    private View fragView;
    private ExpandableListView expListView;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataGroup;
    private HashMap<String, List<Food>>listDataChild;
    private Display display;
    private Point size;
    int width;
    private int lastExpandedPosition = -1;
    private String restaurantID;
    private Order order;
    private Interface listener;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        fragView = inflater.inflate(R.layout.menu_fragment_layout,container,false );

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
        listAdapter = new ExpandableListAdapter(hostActivity, listDataGroup, listDataChild, order);

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
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants").child(restaurantID).child("Menu");
        listDataGroup = new ArrayList<>();
        listDataChild = new HashMap<>();
        reference.addChildEventListener(new ChildEventListener() {
            int counter = 0;
            List<Food>childItem;
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                listDataGroup.add(dataSnapshot.getKey());
                childItem = new ArrayList<>();

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String foodName = (String)ds.getKey();
                    // TODO: Make it dynamic with image
                    SerialBitmap img = new SerialBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.image_empty));
                    Food f = new Food(img, foodName,ds.child("Description").getValue().toString(),Double.parseDouble(ds.child("Price").getValue().toString()),Integer.parseInt(ds.child("Quantity").getValue().toString()));
                    childItem.add(f);
                }
            listDataChild.put(listDataGroup.get(counter), childItem);
                counter++;

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
