package com.example.poleato;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.poleato.Customer;
import com.example.poleato.R;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

public class ReservationExpandableListAdapter {
    Activity host;
    private final LayoutInflater inf;
    private List<String> _listDataGroup; // header titles
    private HashMap<String, List<Customer>> _listDataChild; // child data in format of header title, child title

    public ExpandableListAdapter(Activity host, List<String> listDataHeader,
                                 HashMap<String, List<Customer>> listChildData) {

        this.host = host;
        inf = LayoutInflater.from(host);
        this._listDataGroup = listDataHeader;
        this._listDataChild = listChildData;
    }




    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        DishViewHolder holder; //recycler view pattern

        if (convertView == null){
            convertView = inf.inflate(R.layout.reservation_dishrow_layout, parent, false);
            holder = new DishViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.tv_dish_name);
            holder.quantity = (TextView) convertView.findViewById(R.id.tv_dish_quantity);
            holder.notes = (TextView) convertView.findViewById(R.id.tv_dish_note);
            convertView.setTag(holder);
        } else{
            holder = (DishViewHolder) convertView.getTag();
        }

        holder.name.setText(getChild(groupPosition, childPosition).getName());
        holder.quantity.setText(getChild(groupPosition, childPosition).get);
        //price and currency
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
        String priceStr = decimalFormat.format(getChild(groupPosition, childPosition).getPrice());
        String currency = host.getString(R.string.currency);
        priceStr += currency;
        holder.price.setText(priceStr);
        //quantity
        String qntStr = "(qty "+getChild(groupPosition, childPosition).getQuantity()+")";
        holder.quantity.setText(qntStr);



        //if(childPosition == 0)


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
    public Customer getChild(int groupPosition, int childPosition) {
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


    private class DishViewHolder {
        TextView notes;
        TextView name;
        TextView quantity;
    }

    private class ViewHolder{
        TextView text;
    }

}

