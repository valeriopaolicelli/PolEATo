package com.mad.poleato;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class MenuExpandableListAdapter extends BaseExpandableListAdapter {

    private final Activity host;
    private final LayoutInflater inf;
    private List<String> _listDataGroup; // header titles
    private HashMap<String, List<Food>> _listDataChild; // child data in format of header title, child title
    private Order order;

    public MenuExpandableListAdapter(Activity host, List<String> listDataHeader,
                                     HashMap<String, List<Food>> listChildData, Order order) {

        this.host = host;
        inf = LayoutInflater.from(host);
        this._listDataGroup = listDataHeader;
        this._listDataChild = listChildData;
        this.order = order;
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

    public void updateLitDataChild(){
        for(String s: _listDataChild.keySet()){
            for(Food f : _listDataChild.get(s)){
                if(order.getSelectedFoods().containsKey(f.getFoodID()))
                    f.setSelectedQuantity(order.getSelectedFoods().get(f.getFoodID()).getSelectedQuantity());
                else
                    f.setSelectedQuantity(0);
            }
        }
        notifyDataSetChanged();
    }

    public void refresh(){
        notifyDataSetChanged();
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Log.d("matte", "[Child]getview{ group:"+groupPosition+", child:"+childPosition+", view:"+convertView+"}");

        FoodViewHolder holder; //recycler view pattern

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
            convertView.setTag(holder);

        } else{
            holder = (FoodViewHolder) convertView.getTag();
        }

        final Food  food = getChild(groupPosition,childPosition);

       // holder.img.setImageBitmap(food.getImg().getBitmap());
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
                    order.getSelectedFoods().get(food.getFoodID()).setSelectedQuantity(food.getSelectedQuantity());
                    order.updateTotalPrice();
                    //((OrderActivity)host).setOrder(order); //works but it's bad programming => better use interfaces
                    Log.d("fabio", "new total price: "+ order.getTotalPrice());
                    Toast.makeText(host,"Added to cart",Toast.LENGTH_LONG ).show();
                    notifyDataSetChanged();
                }
                else
                    Toast.makeText(host,"Max quantity reached",Toast.LENGTH_SHORT ).show();
            }
        });

        holder.decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedQuantity = food.getSelectedQuantity();
                if(selectedQuantity>0){
                   food.decreaseSelectedQuantity();
                    if(food.getSelectedQuantity()==0){
                        order.removeFoodFromOrder(food);
                    }
                    else
                        order.getSelectedFoods().get(food.getFoodID()).setSelectedQuantity(food.getSelectedQuantity());

                    order.updateTotalPrice();
                    Log.d("fabio", "new total price: "+ order.getTotalPrice());
                    // ((OrderActivity)host).setOrder(order);
                    Toast.makeText(host,"Removed from cart",Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }
            }
        });

        return convertView; }

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
    }

    private class ViewHolder{
        TextView text;
    }

}
