package com.example.poleato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
    //TODO change text button when status changes
    private Context context;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>>listHashMap;

    public ReservationExpandableListAdapter(Context context, List<Reservation> reservations, HashMap<String, List<Dish>> listHashMap) {
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
        Reservation c = (Reservation) getGroup(i);

        if( view ==  null){
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_row1_layout,null);
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
        boolean flag = false;
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

        final Button button = (Button) view.findViewById(R.id.myButton);

        final Reservation c= reservations.get(i);
        //true => order on delivery or rejected, restaurateur can't do nothing anymore
        if(c.getStatus() == Status.REJECTED)
            flag = true;

        if(isLast && !flag){
            button.setVisibility(View.VISIBLE);
            //if is the last child, add the button "accept or reject" on the bottom
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

                    if(c.getStatus() == Status.DELIVERY){
                        builder.setTitle("Deliver details");
                        String msg= v.getResources().getString(R.string.order) + ": " + c.getOrder_id() + "\n"
                                  + v.getResources().getString(R.string.date) + ": " + c.getDate() + " "
                                  + v.getResources().getString(R.string.time) + ": " + c.getTime() + "\n"
                                  + v.getResources().getString(R.string.surname) + ": " + c.getSurname() + "\n"
                                  + v.getResources().getString(R.string.address) + ": " + c.getAddress() + "\n";
                        builder.setMessage(msg);
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

                            builder.setMessage("Is everything ready? Status will pass to 'on delivery'");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    c.setStatus(Status.DELIVERY, context);
                                    button.setText(context.getString(R.string.order_info));
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

                            builder.setMessage("Do you want to confirm or reject this order? Status will pass to 'on cooking'");

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Change button text
                                    c.setStatus(Status.COOKING, context);
                                    button.setText("Deliver order");
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

}