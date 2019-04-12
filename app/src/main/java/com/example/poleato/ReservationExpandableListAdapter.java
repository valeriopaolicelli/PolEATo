package com.example.poleato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import static android.view.View.GONE;

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
        final Reservation c = (Reservation) getGroup(i);
        ExpandableListView listView = (ExpandableListView) viewGroup;
        final ViewHolder holder;
        boolean flag = false;
        if( view ==  null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_row1_layout,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_status = (TextView) view.findViewById(R.id.tvStatusField);
            holder.button = (Button) view.findViewById(R.id.myButton);
            holder.button.setVisibility(GONE);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.tv_date.setText(c.getDate());
        holder.tv_time.setText(c.getTime());
        holder.tv_status.setText(c.getStat());
        holder.button.setText(c.getButtonText());

        if(!listView.isGroupExpanded(i))
            holder.button.setVisibility(GONE);
        else
            holder.button.setVisibility(View.VISIBLE);

        if (c.getStatus() == Status.REJECTED)
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
            else if (c.getStatus() == Status.DELIVERY)
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));

                if (c.getStatus() == Status.REJECTED)
                    flag = true;

                if (!flag) {
                    //if is the last child, add the button "accept or reject" on the bottom
                    holder.button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            //TODO transfer all strings to file
                            if (c.getStatus() == Status.DELIVERY) {
                                builder.setTitle(context.getString(R.string.title_deliver));
                                String msg = v.getResources().getString(R.string.order) + ": " + c.getOrder_id() + "\n"
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
                            } else {
                                //se lo status è COOKING, il ristoratore può scegliere se far partire la consegna
                                if (c.getStatus() == Status.COOKING) {
                                    builder.setTitle(context.getString(R.string.title_deliver));

                                    builder.setMessage(context.getString(R.string.msg_deliver));
                                    builder.setPositiveButton(context.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            c.setStatus(Status.DELIVERY, context);
                                            holder.button.setText(context.getString(R.string.order_info));
                                            c.setButtonText(context.getString(R.string.order_info));
                                            notifyDataSetChanged();
                                        }
                                    });
                                    builder.setNegativeButton(context.getString(R.string.choice_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.cancel();
                                        }
                                    });
                                } else {
                                    //Se lo stato è ACCEPTANCE, il ristoratore può accetare o rifiutare l'ordine
                                    builder.setTitle(context.getString(R.string.title_confirm));

                                    builder.setMessage(context.getString(R.string.msg_confirm));

                                    builder.setPositiveButton(context.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            // Change button text
                                            c.setStatus(Status.COOKING, context);
                                            holder.button.setText(context.getString(R.string.title_deliver));
                                            c.setButtonText(context.getString(R.string.title_deliver));
                                            notifyDataSetChanged();

                                            //TODO: Aggiornare quantità menù
                                        }
                                    });
                                    builder.setNegativeButton(context.getString(R.string.choice_reject), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            c.setStatus(Status.REJECTED, context);
                                            notifyDataSetChanged();
                                        }
                                    });
                                    builder.setNeutralButton(context.getString(R.string.choice_cancel), new DialogInterface.OnClickListener() {
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
                    holder.button.setVisibility(GONE);
                }

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLast, View view, final ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        ViewHolderChild holder;
        if(view == null){
            holder= new ViewHolderChild();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_dishrow_layout,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes = (TextView) view.findViewById(R.id.tv_dish_note);
            view.setTag(holder);
        }else{
            holder= (ViewHolderChild) view.getTag();
        }


        if(holder.tv_dish_name!=null && holder.tv_dish_notes!=null && holder.tv_dish_quantity!=null) {
            holder.tv_dish_name.setText(dish.getName());
            holder.tv_dish_quantity.setText(dish.getQuantity().toString());
            holder.tv_dish_notes.setText(dish.getNotes());
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

    private class ViewHolder {
        protected Button button;
        TextView tv_date;
        TextView tv_time;
        TextView tv_status;
    }

    private class ViewHolderChild{
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;
    }
}