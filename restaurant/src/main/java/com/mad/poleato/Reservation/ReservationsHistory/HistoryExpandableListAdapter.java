package com.mad.poleato.Reservation.ReservationsHistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mad.poleato.R;
import com.mad.poleato.Reservation.Dish;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.Status;

import java.util.HashMap;
import java.util.List;

public class HistoryExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Reservation> reservations;
    private HashMap<String,List<Dish>>listHashMap;

    private String loggedID;

    public HistoryExpandableListAdapter(Context context, List<Reservation>reservations, HashMap<String,List<Dish>>listHashMap, String currentUserID){
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
        this.loggedID= currentUserID;
    }

    public void updateReservationList(List<Reservation>reservations, HashMap<String,List<Dish>>listHashMap){
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
        final Reservation r = (Reservation) getGroup(i);
        final ViewHolder holder;
        final List<Dish> dishes = r.getDishes();
        boolean flag = false;;

        if( view == null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.history_group_layout,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_status = (TextView) view.findViewById(R.id.tvStatusField);

            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_date.setText(r.getDate());
        holder.tv_time.setText(r.getTime());
        holder.tv_status.setText(r.getStat());

        if (r.getStatus() == Status.REJECTED || r.getStatus() == Status.FAILED) {
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
        }
        else if (r.getStatus() == Status.DELIVERED || r.getStatus() == Status.PAID) {
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
        }
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        final Dish dish = (Dish) getChild(i,i1);
        final Reservation c= reservations.get(i);
        final ChildHolder holder;
        final int group_pos = i, child_pos =i1;

        if(view == null){
            holder= new ChildHolder();

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.history_child_layout,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);
            view.setTag(holder);
        }
        else {
            holder = ((ChildHolder) view.getTag());
        }
        holder.tv_dish_name.setText(dish.getName());
        holder.tv_dish_quantity.setText(dish.getQuantity().toString());
        holder.tv_dish_notes.setText(dish.getNotes());


        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private class ViewHolder {
        TextView tv_date;
        TextView tv_time;
        TextView tv_status;
    }
        private class ChildHolder{
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;
        }

}
