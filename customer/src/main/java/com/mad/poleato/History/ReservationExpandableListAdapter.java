package com.mad.poleato.History;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.mad.poleato.Classes.Dish;
import com.mad.poleato.R;
import com.mad.poleato.Classes.Reservation;

import java.util.HashMap;
import java.util.List;


public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>>listHashMap;
    private String loggedID;

    public ReservationExpandableListAdapter(Context context, List<Reservation> reservations, HashMap<String, List<Dish>> listHashMap, String currentUserID) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
        this.loggedID= currentUserID;
    }


    @Override
    public int getGroupCount() {
        return reservations.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHashMap.get(reservations.get(i).getOrderID()).size();
    }

    @Override
    public Object getGroup(int i) {
        return reservations.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHashMap.get(reservations.get(i).getOrderID()).get(i1); //i = group item, i1 = child item
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
        final Reservation c = (Reservation) getGroup(i);
        final ViewHolder holder;
        if( view ==  null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_group,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_restaurant_name= (TextView)view.findViewById(R.id.tvRestaurantField);
            holder.tv_total_price= (TextView) view.findViewById(R.id.tvTotalPrice);
            holder.tv_review = (TextView) view.findViewById(R.id.reviewtv);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        String price= c.getTotalPrice() + "€";
        //Check if this order has been reviewed
        if(c.isReviewFlag()) {
            holder.tv_review.setText("Reviewed!");
            holder.tv_review.setClickable(false);
            holder.tv_review.setTextColor(Color.GREEN);
        }
        else {
            holder.tv_review.setText("Leave a review");
            holder.tv_review.setClickable(true);
            holder.tv_review.setTextColor(context.getResources().getColor(R.color.colorTextField));
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground,outValue,true);
            holder.tv_review.setBackgroundResource(outValue.resourceId);
            holder.tv_review.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO: start RatingFragment and pass with bundle restaurantID,restaurantName and orderID
                    //TODO: use getter of reservation object to do so with keys equals to the string name
                    Bundle bundle = new Bundle();
                    bundle.putString("restaurantID",c.getRestaurantID());
                    bundle.putString("restaurantName",c.getRestaurantName());
                    bundle.putString("orderID",c.getOrderID());

                    Navigation.findNavController(view).navigate(R.id.action_holder_history_id_to_ratingFragment,bundle);
                    Toast.makeText(context, "Review Text Clicked",Toast.LENGTH_SHORT).show();
                }
            });
        }
        holder.tv_date.setText(c.getDate());
        holder.tv_time.setText(c.getTime());
        holder.tv_total_price.setText(price);
        holder.tv_restaurant_name.setText(c.getRestaurantName());
        notifyDataSetChanged();
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLast, View view, final ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        final Reservation c= reservations.get(i);
        final ChildHolder holder;

        if(view == null){
            holder= new ChildHolder();

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_child,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);

            view.setTag(holder);
        }
        else {
            holder = ((ChildHolder)view.getTag());
        }

        holder.tv_dish_name.setText(dish.getName());
        holder.tv_dish_quantity.setText(dish.getQuantity().toString());
        holder.tv_dish_notes.setText(dish.getNotes());
        notifyDataSetChanged();
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void updateReservations(List<Reservation> reservations) {
        this.reservations=reservations;
    }

    private class ViewHolder {
        TextView tv_date;
        TextView tv_time;
        TextView tv_restaurant_name;
        TextView tv_total_price;
        TextView tv_review;
    }

    private class ChildHolder{
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;

    }
}