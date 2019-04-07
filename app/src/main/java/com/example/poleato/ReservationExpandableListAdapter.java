package com.example.poleato;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Customer> reservations;
    private HashMap<String, List<Dish>>listHashMap;

    public ReservationExpandableListAdapter(Context context, List<Customer> reservations, HashMap<String, List<Dish>> listHashMap) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return reservations.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHashMap.get(reservations.get(i).getOrder_id()).size();
    }

    @Override
    public Object getGroup(int i) {
        return reservations.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHashMap.get(reservations.get(i).getOrder_id()).get(i1); //i = group item, i1 = child item
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Customer c = (Customer) getGroup(i);
        if( view ==  null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_row1_layout,null);
        }
        TextView tv_date = (TextView) view.findViewById(R.id.tvDateField);
        TextView tv_time = (TextView) view.findViewById(R.id.tvTimeField);
        TextView tv_status = (TextView) view.findViewById(R.id.tvStatusField);
//TODO time is not showed
//TODO arrow of listview group should be on the right and more visible
        tv_date.setText(c.getDate());
        tv_time.setText(c.getTime());
        tv_status.setText(c.getStat());

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_dishrow_layout,null);
        }

        TextView tv_dish_name = (TextView) view.findViewById(R.id.tv_dish_name);
        TextView tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
        TextView tv_dish_notes = (TextView) view.findViewById(R.id.tv_dish_note);

        tv_dish_name.setText(dish.getName());
        tv_dish_quantity.setText(dish.getQuantity().toString());
        tv_dish_notes.setText(dish.getNotes());

        Button button = (Button) view.findViewById(R.id.myButton);
        if(b){
            //if is the last child, add the button "accept or reject" on the bottom
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }
}
