package com.mad.poleato;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import java.util.List;
import java.util.zip.Inflater;


public class RestaurantListAdapter extends ArrayAdapter<Restaurant> {

    private final Context context;
    private final LayoutInflater inf;

    public RestaurantListAdapter(Context context, int resource, List<Restaurant> objects) {
        super(context, resource, objects);
        this.context = context;
        inf = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder; //recycler view pattern
        Restaurant r = getItem(position);

        if (convertView == null){
            convertView = inf.inflate(R.layout.restaurant_cardview, parent, false);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.restaurantName);
            holder.img = convertView.findViewById(R.id.restaurantImage);
            holder.isOpen = convertView.findViewById(R.id.restaurantOpen);
            holder.type = convertView.findViewById(R.id.restaurantType);
            convertView.setTag(holder);
        } else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.img.setImageBitmap(r.getImage());
        holder.name.setText(r.getName());
        holder.type.setText(r.getType());
        if(r.getIsOpen())
        {
            holder.isOpen.setText(context.getString(R.string.open));
            holder.isOpen.setTextColor(Color.rgb(0, 200, 0));
        }
        else{
            holder.isOpen.setText(context.getString(R.string.close));
            holder.isOpen.setTextColor(Color.rgb(200, 0, 0));
        }

        return convertView;
    }

    private class ViewHolder{
        ImageView img;
        TextView name;
        TextView type;
        TextView isOpen;
    }
}
