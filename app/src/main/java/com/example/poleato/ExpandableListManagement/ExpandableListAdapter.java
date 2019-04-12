package com.example.poleato.ExpandableListManagement;

import android.app.Activity;
import android.content.res.Resources;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.poleato.DailyOfferFragment;
import com.example.poleato.Food;
import com.example.poleato.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Activity host;
    private final LayoutInflater inf;
    private List<String> _listDataGroup; // header titles
    private HashMap<String, List<Food>> _listDataChild; // child data in format of header title, child title
    private Button button_delete;
    private DailyOfferFragment.FragmentShowEditListener frShow; //to notify to open the EditFoodFragment

    public ExpandableListAdapter(Activity host, List<String> listDataHeader,
                                 HashMap<String, List<Food>> listChildData,
                                 DailyOfferFragment.FragmentShowEditListener frShow) {

        this.host = host;
        inf = LayoutInflater.from(host);
        this._listDataGroup = listDataHeader;
        this._listDataChild = listChildData;
        this.frShow = frShow;
        Log.d("matte", "[Init]headers:"+_listDataGroup.toString());
        Log.d("matte", "[Init]childs:"+_listDataChild.toString());
    }



    public void insertChild(final int groupPosition, Food food){
        this._listDataChild.get(getGroup(groupPosition).toString()).add(food);
        notifyDataSetChanged();
    }

    public void insertChild(String groupTag, Food food){
        this._listDataChild.get(groupTag).add(food);
        notifyDataSetChanged();
    }

    public void removeChild(final int groupPosition, final int childPosition){
        this._listDataChild.get(getGroup(groupPosition).toString()).remove(childPosition);
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
            holder.quantity = (TextView) convertView.findViewById(R.id.cardQuantity);
            convertView.setTag(holder);

        } else{
            holder = (FoodViewHolder) convertView.getTag();
        }

        holder.img.setImageBitmap(getChild(groupPosition, childPosition).getImg());
        holder.name.setText(getChild(groupPosition, childPosition).getName());
        holder.description.setText(getChild(groupPosition, childPosition).getDescription());
        //price and currency
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
        String priceStr = decimalFormat.format(getChild(groupPosition, childPosition).getPrice());
        String currency = host.getString(R.string.currency);
        priceStr += currency;
        holder.price.setText(priceStr);
        //quantity
        String qntStr = "(qty "+getChild(groupPosition, childPosition).getQuantity()+")";
        holder.quantity.setText(qntStr);

        // three dots vertical menu
        ImageButton settingsButton = convertView.findViewById(R.id.cardSettings);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("matte", "inside showPopup");
                PopupMenu popup = new PopupMenu(host, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_cardview, popup.getMenu());
                popup.show();
                Menu itemList = popup.getMenu();


                MenuItem modify = itemList.getItem(0);
                modify.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Log.d("matte", item.getTitle().toString());

                        Food f = getChild(groupPosition, childPosition);
                        frShow.onInputShowEditSent(f);

                        return false;
                    }
                });

                MenuItem remove = itemList.getItem(1);
                remove.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Log.d("matte", item.getTitle().toString());
                        removeChild(groupPosition, childPosition);
                        return true;
                    }
                });

            }
        });

        return convertView;
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


    private class FoodViewHolder {
        ImageView img;
        TextView name;
        TextView description;
        TextView price;
        TextView quantity;
    }

    private class ViewHolder{
        TextView text;
    }

}



