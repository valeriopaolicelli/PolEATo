package com.example.poleato;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
    //TODO change text button when status changes
    private Context context;
    private List<Customer> reservations;
    private HashMap<String, List<Dish>>listHashMap;

    public ReservationExpandableListAdapter(Context context, List<Customer> reservations, HashMap<String, List<Dish>> listHashMap) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return reservations.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHashMap.get(reservations.get(i).getOrder_id()).size();
    }

    @Override
    public Object getGroup(int i) {
        return reservations.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHashMap.get(reservations.get(i).getOrder_id()).get(i1); //i = group item, i1 = child item
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        Customer c = (Customer) getGroup(i);
        ViewHolder holder;         // To handle the button 'accept or reject', new private class (ViewHolder) is created

        if( view ==  null){
            holder= new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_row1_layout,null);
            holder.button= view.findViewById(R.id.myButton);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        TextView tv_date = (TextView) view.findViewById(R.id.tvDateField);
        TextView tv_time = (TextView) view.findViewById(R.id.tvTimeField);
        TextView tv_status = (TextView) view.findViewById(R.id.tvStatusField);

        tv_date.setText(c.getDate());
        tv_time.setText(c.getTime());
        tv_status.setText(c.getStat());
        if(tv_status.getText().equals(context.getString(R.string.reject))) {
            tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
        }
        else if(tv_status.getText().equals(context.getString(R.string.delivery))){
            tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
        }

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLast, View view, final ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_dishrow_layout,null);
        }

        TextView tv_dish_name = (TextView) view.findViewById(R.id.tv_dish_name);
        TextView tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
        TextView tv_dish_notes = (TextView) view.findViewById(R.id.tv_dish_note);

        tv_dish_name.setText(dish.getName());
        tv_dish_quantity.setText(dish.getQuantity().toString());
        tv_dish_notes.setText(dish.getNotes());

        Button button = (Button) view.findViewById(R.id.myButton);

        final int pos= i; // position in reservation.get must be final
        if(isLast){
            //if is the last child, add the button "accept or reject" on the bottom
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View v) {
                    final Customer c= reservations.get(pos);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    Log.d("Valerio","Old status: " + c.getStatus().toString());

                    if(c.getStatus() == Status.REJECTED){
                        builder.setTitle("Order Rejected");

                        builder.setMessage("Sorry, you've already rejected this order");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    }
                    else if(c.getStatus() == Status.DELIVERY){
                        builder.setTitle("Order Delivered");

                        builder.setMessage("You've already delivered this order");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    }
                    else {

                        //se lo status è COOKING, il ristoratore può scegliere se far partire la consegna
                        if(c.getStatus() == Status.COOKING){
                            builder.setTitle("Deliver order");

                            builder.setMessage("Is everything ready? Status will pass into 'on delivery'");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    c.setStatus(Status.DELIVERY, context);
                                    notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                        }
                        else {
                            //Se lo stato è ACCEPTANCE, il ristoratore può accetare o rifiutare l'ordine
                            builder.setTitle("Confirm order");

                            builder.setMessage("Do you want to confirm or reject this order? Status will pass into 'on cooking'");

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    c.setStatus(Status.COOKING, context);
                                    notifyDataSetChanged();

                                    //TODO: Aggiornare quantità menù
                                }
                            });
                            builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    c.setStatus(Status.REJECTED, context);
                                    notifyDataSetChanged();
                                }
                            });
                            builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                        }
                    }
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private class ViewHolder{
        Button button;
    }
}