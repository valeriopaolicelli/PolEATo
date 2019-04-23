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

import java.util.ArrayList;
import java.util.List;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantRecyclerViewAdapter.RestaurantViewHolder> {


    private List<Restaurant> resList;
    private Context context;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {

        public TextView title, type, open;
        public ImageView img;
        public View itemView;

        public RestaurantViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.title = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.type = (TextView) itemView.findViewById(R.id.textViewType);
            this.open = (TextView) itemView.findViewById(R.id.textViewOpen);
            this.img = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RestaurantRecyclerViewAdapter(Context context, List<Restaurant> resList) {
        this.resList = resList;
        this.context = context;
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
        holder.title.setText(resList.get(position).getName());
        holder.type.setText(resList.get(position).getType());
        holder.img.setImageBitmap(resList.get(position).getImage());

        if(resList.get(position).getIsOpen())
        {
            holder.open.setText(context.getString(R.string.open));
            holder.open.setTextColor(Color.rgb(0, 200, 0));
        }
        else{
            holder.open.setText(context.getString(R.string.close));
            holder.open.setTextColor(Color.rgb(200, 0, 0));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("matte", "OnClick | restaurant ID: "+resList.get(position).getId());
                Bundle bundle = new Bundle();
                bundle.putString("id", resList.get(position).getId());
                //Intent menuIntent = new Intent(context, /*TODO FABIO*/);
                //menuIntent.putExtras(bundle);
            }
        });



    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return resList.size();
    }

    public void setResList(List<Restaurant> list){
        this.resList = list;
        notifyDataSetChanged();
    }


}
