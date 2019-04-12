package com.example.poleato;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
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
        final ViewHolder holder;
        boolean buttonflag = false;
        ExpandableListView listView = (ExpandableListView) viewGroup;
        List<Dish> dishes = listHashMap.get(c.getOrder_id());

        final int position = i;
        boolean flag = false;
        if( view ==  null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_row1_layout,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_status = (TextView) view.findViewById(R.id.tvStatusField);
            holder.button = (Button) view.findViewById(R.id.myButton);
            holder.selectAllCheckBox = (CheckBox) view.findViewById(R.id.selectAllCheckBox);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
            holder.tv_date.setText(c.getDate());
            holder.tv_time.setText(c.getTime());
            holder.tv_status.setText(c.getStat());

        if (c.getStatus() == Status.REJECTED) {
            flag = true;
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
        }
            else if (c.getStatus() == Status.DELIVERY)
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
            else if (c.getStatus() == Status.ACCEPATANCE ) {
                holder.button.setText("Accept or reject");
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
                holder.button.setVisibility(View.VISIBLE);
        }
        // Se lo stato è COOKING allora compare la checkbox
        if(c.getStatus() == Status.COOKING) {
            holder.button.setText("Deliver Order");
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
            holder.selectAllCheckBox.setVisibility(View.VISIBLE);
            assert dishes != null;

            if(c.isChecked())
                buttonflag=true;
            else {
                for (Dish d : dishes) {
                    if (d.isChecked())
                        //Se tutti i piatti sono pronti allora l'ordine può partire
                        buttonflag = true;
                    else {
                        buttonflag = false;
                        break;
                    }
                }
            }
        }
        else
            holder.selectAllCheckBox.setVisibility(View.GONE);



        holder.selectAllCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(holder.selectAllCheckBox.isChecked()){
                    c.setChecked(true);
                }
                else
                    c.setChecked(false);
                notifyDataSetChanged();
            }
        });

        final boolean bflag = buttonflag;
        if (!flag) {
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    if (c.getStatus() == Status.DELIVERY) {
                        builder.setTitle("Deliver details");
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
                            builder.setTitle("Deliver order");

                            if(bflag)
                            builder.setMessage("Is everything ready? Status will pass to 'on delivery'");
                            else
                                builder.setMessage("Not all dishes are checked. Do you want to " +
                                        "start your delivery anyway?");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    c.setStatus(Status.DELIVERY, context);
                                    holder.button.setText(context.getString(R.string.order_info));
                                    notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                        } else {
                            //Se lo stato è ACCEPTANCE, il ristoratore può accetare o rifiutare l'ordine
                            builder.setTitle("Confirm order");

                            builder.setMessage("Do you want to confirm or reject this order? Status will pass to 'on cooking'");

                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Change button text
                                    c.setStatus(Status.COOKING, context);
                                    holder.button.setText("Deliver order");
                                    notifyDataSetChanged();
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
            holder.button.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLast, View view, final ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        final Reservation c= reservations.get(i);

        ExpandableListView listView = (ExpandableListView) viewGroup;
        List<Dish> dishes = listHashMap.get(c.getOrder_id());
        final ChildHolder holder;
        boolean buttonflag = false;

        //CALCOLO POSIZIONE GRUPPO E RELATIVA VIEW
        long packedPosition =  ExpandableListView.getPackedPositionForGroup(i);
        int flatPosition = listView.getFlatListPosition(packedPosition);
        int first  = listView.getFirstVisiblePosition();
        View group = listView.getChildAt(flatPosition-first);

        if(view == null){
            holder= new ChildHolder();

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.reservation_dishrow_layout,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);
            holder.dish_chechbox= (CheckBox) view.findViewById(R.id.dish_checkbox);
            holder.group_checkbox= (CheckBox) group.findViewById(R.id.selectAllCheckBox);

            view.setTag(holder);
        }
        else
            holder= (ChildHolder) view.getTag();

        holder.tv_dish_name.setText(dish.getName());
        holder.tv_dish_quantity.setText(dish.getQuantity().toString());
        holder.tv_dish_notes.setText(dish.getNotes());


        if (c.getStatus() == Status.COOKING) {
            //Se lo stato della prenotazione è COOKING, rendo la checkbox visibile
            holder.dish_chechbox.setVisibility(View.VISIBLE);
            // Controllo se la prentazione ha il boolean  check su TRUE
            if (c.isChecked()) {
                // Se così fosse, allora  la checkbox "select all" è stata spuntata
                // Controllo se il piatto ha la checkbox spuntata, la spunto se così non fosse
                if (!holder.dish_chechbox.isChecked()) {
                    holder.dish_chechbox.setChecked(true);
                    //Setto lo stato del piatto a pronto => ERRATO: STO MODIFICANDO ANCHE I PIATTI DELLE ALTRE PRENOTAZIONI
                    dish.setChecked(true);
                    notifyDataSetChanged();
                }
            }
        }
        else
            holder.dish_chechbox.setVisibility(View.GONE);

        holder.dish_chechbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(dish.isChecked()){
                    dish.setChecked(false);
                    c.setChecked(false);
                    holder.group_checkbox.setChecked(false);
                    }
                else
                    dish.setChecked(true);
                notifyDataSetChanged();
            }
        });
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
        CheckBox selectAllCheckBox;
        TextView tv_date;
        TextView tv_time;
        TextView tv_status;
        CheckBox dish_chechbox;
    }

    private class ChildHolder{
        Button childButton;
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;
        CheckBox dish_chechbox;
        CheckBox group_checkbox;
    }
}
