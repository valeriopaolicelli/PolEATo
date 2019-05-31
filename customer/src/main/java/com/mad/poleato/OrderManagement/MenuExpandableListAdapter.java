package com.mad.poleato.OrderManagement;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.Classes.Food;
import com.mad.poleato.Interface;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Adapter for the ExpandableList of the restaurant's menu
 */
public class MenuExpandableListAdapter extends BaseExpandableListAdapter {

    private Toast myToast;

    private final Activity host;
    private final LayoutInflater inf;
    private List<String> _listDataGroup; // header titles
    private HashMap<String, List<Food>> _listDataChild; // child data in format of header title, child title
    private Order order;
    private Interface listener;
    private String currentUserID;
    private FirebaseAuth mAuth;

    public MenuExpandableListAdapter(Activity host, List<String> listDataHeader,
                                     HashMap<String, List<Food>> listChildData, Order order, Interface listener) {

        myToast = Toast.makeText(host, "", Toast.LENGTH_SHORT);

        this.host = host;
        inf = LayoutInflater.from(host);
        this._listDataGroup = listDataHeader;
        this._listDataChild = listChildData;
        this.order = order;
        this.listener = listener;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        Log.d("matte", "[Init]headers:"+_listDataGroup.toString());
        Log.d("matte", "[Init]childs:"+_listDataChild.toString());
    }



    public void insertChild(String groupTag, Food food){
        this._listDataChild.get(groupTag).add(food);
        notifyDataSetChanged();
    }


    public void setOrder(Order order){
        this.order=order;
    }

    /**
     * Method used to update the collection of the adapter
     */
     void updateLitDataChild(){
        for(String s: _listDataChild.keySet()){
            for(Food f : Objects.requireNonNull(_listDataChild.get(s))){
                if(order.getSelectedFoods().containsKey(f.getFoodID()))
                    f.setSelectedQuantity(Objects.requireNonNull(order.getSelectedFoods().get(f.getFoodID())).getSelectedQuantity());
                else
                    f.setSelectedQuantity(0);
            }
        }
        notifyDataSetChanged();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Log.d("matte", "[Child]getview{ group:"+groupPosition+", child:"+childPosition+", view:"+convertView+"}");

        final FoodViewHolder holder; //recycler view pattern

        if (convertView == null){
            convertView = inf.inflate(R.layout.layout_menu_child, parent, false);
            holder = new FoodViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.cardImage);
            holder.name = (TextView) convertView.findViewById(R.id.cardName);
            holder.description = (TextView) convertView.findViewById(R.id.cardDescription);
            holder.price = (TextView) convertView.findViewById(R.id.cardPrice);
            holder.increase = (ImageButton) convertView.findViewById(R.id.increaseBtn);
            holder.decrease = (ImageButton) convertView.findViewById(R.id.decreaseBtn);
            holder.selectedQuantity = (TextView) convertView.findViewById(R.id.quantity);

            // initialize attributes for favorite toggle
            holder.buttonFavorite= (ToggleButton) convertView.findViewById(R.id.button_add_favorite_dish);
            holder.scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
            holder.scaleAnimation.setDuration(500);
            holder.bounceInterpolator = new BounceInterpolator();
            holder.scaleAnimation.setInterpolator(holder.bounceInterpolator);

            convertView.setTag(holder);

        } else{
            holder = (FoodViewHolder) convertView.getTag();
        }

        final Food  food = getChild(groupPosition,childPosition);

        //If food is in the selectedFood of the order, set the quantity selected
        if(order.getSelectedFoods().containsKey(food.getFoodID())){
            food.setSelectedQuantity(order.getSelectedFoods().get(food.getFoodID()).getSelectedQuantity());
        }else{
            food.setSelectedQuantity(0);
        }

        //Using Picasso to download images
        if(food.getImg().equals("")){
            Picasso.with(host.getApplicationContext()).load(R.drawable.plate_fork).into(holder.img);
        }else
            Picasso.with(host.getApplicationContext()).load(food.getImg()).into(holder.img);

        holder.name.setText(food.getName());
        holder.description.setText(food.getDescription());
        //price and currency
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
        String priceStr = decimalFormat.format(food.getPrice());
        String currency = host.getString(R.string.currency);
        priceStr += currency;
        holder.price.setText(priceStr);
        //quantity
        if(food.getSelectedQuantity()==0)
            holder.selectedQuantity.setText(host.getResources().getString(R.string.slash));
        else
            holder.selectedQuantity.setText(Integer.toString(food.getSelectedQuantity()));

        //buttons for handling increase and decrease of quantity
        holder.increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = food.getQuantity();
                int selectedQuantity = food.getSelectedQuantity();
                //check if restaurant has enough quantity requested
                if(selectedQuantity<quantity) {
                    food.increaseSelectedQuantity();

                    if(!order.getSelectedFoods().containsKey(food.getFoodID())) {
                        order.addFoodToOrder(food);
                    }
                    Objects.requireNonNull(order.getSelectedFoods().get(food.getFoodID())).setSelectedQuantity(food.getSelectedQuantity());
                    //Updating order attributes
                    order.updateTotalPrice();
                    order.increaseToTotalQuantity();
                    listener.setQuantity(order.getTotalQuantity());
                    Log.d("fabio", "new total price: "+ order.getTotalQuantity());
                    myToast.setText(host.getString(R.string.added_to_cart));
                    myToast.show();
                    notifyDataSetChanged();
                }
                else{
                    myToast.setText(host.getString(R.string.max_quantity_cart));
                    myToast.show();
                }
            }
        });

        holder.decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedQuantity = food.getSelectedQuantity();
                //Quantity selected cannot be empty
                if(selectedQuantity>0){
                   food.decreaseSelectedQuantity();
                    if(food.getSelectedQuantity()==0){
                        order.removeFoodFromOrder(food);
                    }
                    else
                        Objects.requireNonNull(order.getSelectedFoods().get(food.getFoodID())).setSelectedQuantity(food.getSelectedQuantity());

                    //Updating order attributes
                    order.decreaseToTotalQuantity();
                    Log.d("fabio", "Setting quantity in badge from listAdapter: " + order.getTotalQuantity());
                    listener.setQuantity(order.getTotalQuantity());
                    order.updateTotalPrice();
                    Log.d("fabio", "new total price: "+ order.getTotalPrice());

                    myToast.setText(host.getString(R.string.removed_from_cart));
                    myToast.show();
                    notifyDataSetChanged();
                }
            }
        });

        // animate button favorite
        String restaurantID = order.getRestaurantID();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("customers/" + currentUserID +
                                                                                       "/Favorite/" + restaurantID +
                                                                                       "/dishes");

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String foodID= food.getFoodID();
                    if (dataSnapshot.hasChild(foodID))
                        holder.buttonFavorite.setChecked(true);
                    else
                        holder.buttonFavorite.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.buttonFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //animation
                compoundButton.startAnimation(holder.scaleAnimation);

                // add or remove restaurant from favorite
                if(isChecked){
                    // add restaurant to favorite list
                    String foodID= food.getFoodID();
                    String foodName= food.getName();
                    String restaurantID= order.getRestaurantID();
                    FirebaseDatabase.getInstance().getReference(    "customers/" + currentUserID
                                                                        + "/Favorite/" + restaurantID
                                                                        + "/dishes/" + foodID).setValue(foodName);
                }
                else{
                    // remove restaurant from favorite list
                    String foodID= food.getFoodID();
                    final String restaurantID= order.getRestaurantID();
                    FirebaseDatabase.getInstance().getReference( "customers/" + currentUserID
                                                                     + "/Favorite/" + restaurantID
                                                                     + "/dishes/" + foodID).removeValue();
                    // at this point if this is the last food in the favorite list, also the restaurant is removed from the favorite
                    // but I want that the restaurant remains in the favorite list of restaurants, so I put it again
                    DatabaseReference referenceCustomerFavorite= FirebaseDatabase.getInstance().getReference("customers/" +
                                                                                                                    currentUserID +
                                                                                                                    "/Favorite");

                    referenceCustomerFavorite.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(!dataSnapshot.hasChild(restaurantID)) {
                                // restore restaurant without plates in the favorite list
                                FirebaseDatabase.getInstance().getReference("customers/" +
                                                                                    currentUserID +
                                                                                    "/Favorite/" + restaurantID).setValue("none");
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });

        return convertView;
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);

    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        Log.d("matte", "[Group]getview{ group:"+groupPosition+", view:"+convertView+", name:"+getGroup(groupPosition).toString()+"}");
        ViewHolder holder; //recycler view pattern

        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null){
            convertView = inf.inflate(R.layout.layout_menu_group, parent, false);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.groupView);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(groupTitle);

        return convertView;
    }




    @Override
    public Food getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataGroup.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).hashCode();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataGroup.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this._listDataGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroup(groupPosition).hashCode();
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public Order getOrder(){
        return order;
    }

    private class FoodViewHolder {
        ImageView img;
        TextView name;
        TextView description;
        TextView price;
        TextView selectedQuantity;
        ImageButton decrease;
        ImageButton increase;


        // attributes for togglebutton favorite
        public ScaleAnimation scaleAnimation;
        public BounceInterpolator bounceInterpolator;
        public ToggleButton buttonFavorite;
    }

    private class ViewHolder{
        TextView text;
    }

}
