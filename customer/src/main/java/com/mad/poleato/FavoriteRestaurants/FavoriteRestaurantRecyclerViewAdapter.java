package com.mad.poleato.FavoriteRestaurants;

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
import com.mad.poleato.Classes.Restaurant;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.OrderManagement.OrderActivity;
import com.mad.poleato.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FavoriteRestaurantRecyclerViewAdapter extends RecyclerView.Adapter<FavoriteRestaurantRecyclerViewAdapter.FavoriteRestaurantViewHolder> {


    private List<Restaurant> list; //current displayed list
    private Context context;
    private Toast myToast;

    private String currentUserID;
    private FirebaseAuth mAuth;

    List<MyDatabaseReference> dbReferenceList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class FavoriteRestaurantViewHolder extends RecyclerView.ViewHolder {

        public TextView title, type, delivery, priceRange, open;
        public ImageView img;
        public View itemView;

        // attributes for togglebutton favorite
        public ScaleAnimation scaleAnimation;
        public BounceInterpolator bounceInterpolator;
        public ToggleButton buttonFavorite;

        public FavoriteRestaurantViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;

            this.title = (TextView) itemView.findViewById(R.id.textViewTitle);
            this.type = (TextView) itemView.findViewById(R.id.textViewType);
            this.open = (TextView) itemView.findViewById(R.id.textViewOpen);
            this.img = (ImageView) itemView.findViewById(R.id.imageView);
            this.delivery = (TextView) itemView.findViewById(R.id.textViewDelivery);
            this.priceRange = (TextView) itemView.findViewById(R.id.textViewPriceRange);

            // initialize attributes for favorite toggle
            this.buttonFavorite= (ToggleButton) itemView.findViewById(R.id.button_favorite);
            this.scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
            this.scaleAnimation.setDuration(500);
            this.bounceInterpolator = new BounceInterpolator();
            this.scaleAnimation.setInterpolator(bounceInterpolator);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FavoriteRestaurantRecyclerViewAdapter(Context context, List<Restaurant> list) {

        this.list = list;
        this.context = context;

        if(context != null)
            myToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

        this.dbReferenceList= new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FavoriteRestaurantRecyclerViewAdapter.FavoriteRestaurantViewHolder onCreateViewHolder(ViewGroup parent,
                                                                                         int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        final View listItem = layoutInflater.inflate(R.layout.restaurant_item, parent, false);
        final FavoriteRestaurantViewHolder viewHolder = new FavoriteRestaurantViewHolder(listItem);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final FavoriteRestaurantViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.title.setText(list.get(position).getName());
        holder.type.setText(list.get(position).getType());
        if(list.get(position).getImage().equals("")){
            Picasso.with(context).load(R.drawable.plate_fork).into(holder.img);
        }
        else
            Picasso.with(context).load(list.get(position).getImage()).into(holder.img);
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
                    myToast.setText(context.getString(R.string.closed_restaurant));
                    myToast.show();
                }
            }
        });

        holder.buttonFavorite.setChecked(true);

        holder.buttonFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //animation
                compoundButton.startAnimation(holder.scaleAnimation);

                // add or remove restaurant from favorite
                if(!isChecked){
                    // remove restaurant from favorite list
                    String restaurantID= list.get(position).getId();
                    FirebaseDatabase.getInstance().getReference("customers/"+currentUserID+"/Favorite/"+restaurantID).removeValue();
                }
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        for(int i=0; i < dbReferenceList.size(); i++)
            dbReferenceList.get(i).removeAllListener();
    }
}
