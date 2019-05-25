package com.mad.poleato.AboutUs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.DailyOffer.Food;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PopularFoodsRecyclerViewAdapter extends RecyclerView.Adapter<PopularFoodsRecyclerViewAdapter.PopularFoodsViewHolder> {


    private List<Food> list; //current displayed list
    private Context context;
    private Toast myToast;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class PopularFoodsViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public View itemView;


        public PopularFoodsViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            this.name = (TextView) itemView.findViewById(R.id.cardName);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PopularFoodsRecyclerViewAdapter(Context context, List<Food> list) {

        this.list = list;
        this.context = context;

        if(context != null)
            myToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PopularFoodsRecyclerViewAdapter.PopularFoodsViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                         int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View listItem = layoutInflater.inflate(R.layout.layout_dish_popular, parent, false);
        final PopularFoodsViewHolder viewHolder = new PopularFoodsViewHolder(listItem);

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final PopularFoodsViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.name.setText(list.get(position).getName());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }
}
