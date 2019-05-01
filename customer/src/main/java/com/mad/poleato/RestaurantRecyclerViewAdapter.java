package com.mad.poleato;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantRecyclerViewAdapter.RestaurantViewHolder> {


    private List<Restaurant> list; //current displayed list
    private Context context;
    Toast myToast;

    /**
     * current order state of the list
     */
    private enum State{
        INIT,
        NAME_SORTED,
        PRICE_SORTED,
        PRICE_INVERSE_SORTED,
        DELIVERY_SORTED
    }
    State currState;

    /**
     * Comparators
     */

    private SortByName nameComparator;
    private SortByPrice priceComparator;
    private SortByPriceInverse priceInverseComparator;
    private SortByDelivery deliveryComparator;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

        public TextView title, type, delivery, priceRange, open;
        public ImageView img;
        public View itemView;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.title = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.type = (TextView) itemView.findViewById(R.id.textViewType);
            this.open = (TextView) itemView.findViewById(R.id.textViewOpen);
            this.img = (ImageView) itemView.findViewById(R.id.imageView);
            this.delivery = (TextView) itemView.findViewById(R.id.textViewDelivery);
            this.priceRange = (TextView) itemView.findViewById(R.id.textViewPriceRange);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RestaurantRecyclerViewAdapter(Context context, List<Restaurant> list) {

        this.list = list;
        this.context = context;
        currState = State.INIT;

        if(context != null)
            myToast = Toast.makeText(context, "", Toast.LENGTH_LONG);

        this.nameComparator = new SortByName();
        this.priceComparator = new SortByPrice();
        this.priceInverseComparator = new SortByPriceInverse();
        this.deliveryComparator = new SortByDelivery();

    }

    // Create new views (invoked by the layout manager)
    @Override
    public RestaurantRecyclerViewAdapter.RestaurantViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                 int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View listItem = layoutInflater.inflate(R.layout.restaurant_item, parent, false);
        final RestaurantViewHolder viewHolder = new RestaurantViewHolder(listItem);

        return viewHolder;


    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RestaurantViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(list.get(position).getName());
        holder.type.setText(list.get(position).getType());
        holder.img.setImageBitmap(list.get(position).getImage());
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
        double deliveryCost = list.get(position).getDeliveryCost();
        if(deliveryCost < 0.1)
            holder.delivery.setText(context.getString(R.string.delivery_cost)+":\n"+context.getString(R.string.free_delivery));
        else{
            String priceStr = decimalFormat.format(list.get(position).getDeliveryCost());
            holder.delivery.setText(context.getString(R.string.delivery_cost)+":\n"+priceStr+"€");
        }

        int priceR = list.get(position).getPriceRange();
        String stringR = new String("");
        for(int count = 0; count < priceR; count ++)
            stringR += "$";

        holder.priceRange.setText(context.getString(R.string.price_range)+":\n"+stringR);


        if(list.get(position).getIsOpen())
        {
            holder.open.setText(context.getString(R.string.open));
            holder.open.setTextColor(Color.rgb(0, 100, 0));
        }
        else{
            holder.open.setText(context.getString(R.string.close));
            holder.open.setTextColor(Color.rgb(150, 0, 0));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(list.get(position).getIsOpen()) {
                    Log.d("matte", "OnClick | restaurant ID: " + list.get(position).getId());
                    Bundle bundle = new Bundle();
                    bundle.putString("id", list.get(position).getId());
                    Intent menuIntent = new Intent(context, OrderActivity.class);
                    menuIntent.putExtras(bundle);
                    context.startActivity(menuIntent);
                }
                else
                {
                    myToast.setText("Restaurant is closed at the moment");
                    myToast.show();
                }
            }
        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }




    public void display(List<Restaurant> list) {
        this.list = list;
        notifyDataSetChanged();
    }

            /*  *********************
                ****** SORTING ******
                *********************   */

    //Use the static method `Collectios.sort()` instead of list.sort() which is not supported in API < 24


    public void sortByName(){
        Collections.sort(this.list, this.nameComparator);
        //this.list.sort(this.nameComparator);
        currState = State.NAME_SORTED;
        notifyDataSetChanged();
    }

    public void sortByPrice(){
        Collections.sort(this.list, this.priceComparator);
        //this.list.sort(this.priceComparator);
        currState = State.PRICE_SORTED;
        notifyDataSetChanged();
    }

    public void sortByPriceInverse(){
        Collections.sort(this.list, this.priceInverseComparator);
        //this.list.sort(this.priceInverseComparator);
        currState = State.PRICE_INVERSE_SORTED;
        notifyDataSetChanged();
    }

    public void sortByDelivery(){
        Collections.sort(this.list, this.deliveryComparator);
        //this.list.sort(deliveryComparator);
        currState = State.DELIVERY_SORTED;
        notifyDataSetChanged();
    }

    /* TODO optimize it by using TreeMap with ordered insertion, without sort again every new insertion */
    public void updateLayout(){
        if(currState == State.INIT){
            notifyDataSetChanged();
        }
        else if(currState == State.NAME_SORTED){
            sortByName();
        }
        else if(currState == State.PRICE_SORTED){
            sortByPrice();
        }
        else if(currState == State.PRICE_INVERSE_SORTED){
            sortByPriceInverse();
        }
        else if(currState == State.DELIVERY_SORTED){
            sortByDelivery();
        }

    }



    private class SortByName implements Comparator<Restaurant>{

        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return r1.getName().compareTo(r2.getName());
        }
    }

    private class SortByPrice implements  Comparator<Restaurant>{

        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return r1.getPriceRange() - r2.getPriceRange();
        }
    }

    private class SortByPriceInverse implements Comparator<Restaurant>{

        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return r2.getPriceRange() - r1.getPriceRange();
        }
    }

    private class SortByDelivery implements Comparator<Restaurant>{
        @Override
        public int compare(Restaurant r1, Restaurant r2) {
            return Double.compare(r1.getDeliveryCost(), r2.getDeliveryCost());
        }
    }


}
