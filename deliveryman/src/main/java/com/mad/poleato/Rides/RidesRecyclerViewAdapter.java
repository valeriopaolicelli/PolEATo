package com.mad.poleato.Rides;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.poleato.R;

import java.text.DecimalFormat;
import java.util.List;

public class RidesRecyclerViewAdapter extends RecyclerView.Adapter<RidesRecyclerViewAdapter.RidesViewHolder> {

    private List<Ride>ridesList;
    private Context context;

    public class RidesViewHolder extends RecyclerView.ViewHolder {
        public TextView order_tv, surname_tv, deliveryddress_tv;
        public TextView restaurant_tv, restaurantAddress_tv,totalPrice_tv, dishes_tv;
        public View itemView;

        public RidesViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            this.order_tv = (TextView) itemView.findViewById(R.id.order_tv);
            this.surname_tv = (TextView) itemView.findViewById(R.id.surname_tv);
            this.deliveryddress_tv = (TextView) itemView.findViewById(R.id.deliveryaddress_tv);
            this.restaurant_tv = (TextView) itemView.findViewById(R.id.restaurant_tv);
            this.restaurantAddress_tv = (TextView) itemView.findViewById(R.id.restaurantAddress_tv);
            this.totalPrice_tv = (TextView) itemView.findViewById(R.id.totalPrice_tv);
            this.dishes_tv = (TextView) itemView.findViewById(R.id.dishes_tv);
        }
    }

    public RidesRecyclerViewAdapter(Context context, List<Ride> ridesList){
        this.context=context;
        this.ridesList=ridesList;
    }

    public void setAllRiders(List<Ride> riders) {
        this.ridesList = riders;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RidesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View listItem = layoutInflater.inflate(R.layout.ride_layout, viewGroup, false);
        final RidesViewHolder viewHolder = new RidesViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RidesViewHolder ridesViewHolder, final int position) {
        ridesViewHolder.order_tv.setText(ridesList.get(position).getOrderID());
        ridesViewHolder.surname_tv.setText(ridesList.get(position).getSurname());
        ridesViewHolder.deliveryddress_tv.setText(ridesList.get(position).getDeliveryAddress());
        ridesViewHolder.restaurant_tv.setText(ridesList.get(position).getRestaurantName());
        ridesViewHolder.restaurantAddress_tv.setText(ridesList.get(position).getRestaurantAddress());
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        double totalPrice = ridesList.get(position).getTotalPrice();
        String priceStr = decimalFormat.format(totalPrice).toString()+"€";
        ridesViewHolder.totalPrice_tv.setText(priceStr);
        ridesViewHolder.dishes_tv.setText(ridesList.get(position).getTotalDishes().toString());
    }

    @Override
    public int getItemCount() {
        return ridesList.size();
    }

    public void display(List<Ride> list) {
        this.ridesList = list;
        notifyDataSetChanged();
    }


}
