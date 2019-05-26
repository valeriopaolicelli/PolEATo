package com.mad.poleato.Reservation.ReservationListManagement;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.Dish;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.ReservationFragmentDirections;
import com.mad.poleato.Reservation.Status;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


import static android.view.View.GONE;


public class ReservationExpandableListAdapter extends BaseExpandableListAdapter{
    private Context context;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>>listHashMap; //Map Containg for each order the list of dishes

    private HashMap<String,Boolean>groupChecked = new HashMap<>();  //This map keeps track of checkbox's state foreach group
    private HashMap<String, ArrayList<Boolean>>childsChecked = new HashMap<>(); //This map keep track of checkbox's states of every child for a group
    private HashMap<String, CheckBox> groupCheckBoxes = new HashMap<>(); //Map of groups checkboxes, used to to handle group check box from getChildView

    private String loggedID;

    public ReservationExpandableListAdapter(Context context, List<Reservation> reservations, HashMap<String, List<Dish>> listHashMap, String currentUserID) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
        this.loggedID= currentUserID;
    }

    //This method add add a new value to the collections that handle the check states
    //Only if there is a new reservation
    public void addCheckState(boolean defaultState){
        for (Reservation r : reservations){
            if(!groupChecked.containsKey(r.getOrder_id())){
                groupChecked.put(r.getOrder_id(),defaultState);
                ArrayList<Boolean> childStates = new ArrayList<>();
                for(int j = 0; j < listHashMap.get(r.getOrder_id()).size(); j++){
                    childStates.add(defaultState);
                }
                childsChecked.put(r.getOrder_id(), childStates);
            }
        }
    }

    public void updateReservationList(List<Reservation>reservations, HashMap<String,List<Dish>>listHashMap){
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
    public View getGroupView(int i, boolean b, View view, final ViewGroup viewGroup) {
        final Reservation r = (Reservation) getGroup(i);
        final ViewHolder holder;
        final List<Dish> dishes = r.getDishes();
        boolean flag = false;


        if( view ==  null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_group,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_status = (TextView) view.findViewById(R.id.tvStatusField);
            holder.button = (Button) view.findViewById(R.id.myButton);
            holder.selectAllCheckBox = (CheckBox) view.findViewById(R.id.selectAllCheckBox);
            groupCheckBoxes.put(r.getOrder_id(), holder.selectAllCheckBox);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_date.setText(r.getDate());
        holder.tv_time.setText(r.getTime());
        holder.tv_status.setText(r.getStat());

        if(groupChecked.size()<=i){
            groupChecked.put(r.getOrder_id(),false);
        }else{
            holder.selectAllCheckBox.setChecked(groupChecked.get(r.getOrder_id()));
            groupCheckBoxes.put(r.getOrder_id(), holder.selectAllCheckBox);

        }

        if(r.getStatus() == Status.DELIVERED || r.getStatus() == Status.FAILED){
            //Adding reservation to History
            DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("History");
            dbReference.child(r.getOrder_id()).child("customerID").setValue(r.getCustomerID());
            dbReference.child(r.getOrder_id()).child("date").setValue(r.getDate());
            dbReference.child(r.getOrder_id()).child("time").setValue(r.getTime());
            dbReference.child(r.getOrder_id()).child("totalPrice").setValue(r.getTotalPrice());
            if(r.getStatus() == Status.DELIVERED) {
                dbReference.child(r.getOrder_id()).child("status/it").setValue("Consegnato");
                dbReference.child(r.getOrder_id()).child("status/en").setValue("Delivered");
            }
            else{
                dbReference.child(r.getOrder_id()).child("status/it").setValue("Fallito");
                dbReference.child(r.getOrder_id()).child("status/en").setValue("Failed");
            }
            dbReference.child(r.getOrder_id()).child("dishes").setValue(r.getDishes());

            //Delete reservation from pending reservations
            FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(r.getOrder_id()).removeValue();
        }

        if (r.getStatus() == Status.REJECTED) {
            flag = true;
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
        }
        else if (r.getStatus() == Status.DELIVERY) {
            holder.button.setText(context.getResources().getString(R.string.order_info));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
            holder.button.setVisibility(View.VISIBLE);
        }
        else if (r.getStatus() == Status.ACCEPTANCE ) {
            holder.button.setText(context.getResources().getString(R.string.button_reservation));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
            holder.button.setVisibility(View.VISIBLE);
        }
        // Se lo stato è COOKING allora compare la checkbox
        if(r.getStatus() == Status.COOKING) {
            holder.button.setText(context.getResources().getString(R.string.order_deliver));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
            holder.selectAllCheckBox.setVisibility(View.VISIBLE);
            holder.button.setVisibility(View.VISIBLE);
        }
        else
            holder.selectAllCheckBox.setVisibility(View.GONE);


        final int group_pos = i;

        holder.selectAllCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean state = groupChecked.get(r.getOrder_id());
                groupChecked.put(r.getOrder_id(), state ? false : true);
                groupCheckBoxes.get(r.getOrder_id()).setChecked(state ? false : true);
                ArrayList<Boolean>childs = childsChecked.get(r.getOrder_id());
                for ( int i=0 ; i<listHashMap.get(r.getOrder_id()).size(); i++){
                    childs.set(i, state ? false : true);
                }
                childsChecked.put(r.getOrder_id(),childs);
                Log.d("GroupCheckbox", "Clicked Group checkbox: "+ group_pos);
                notifyDataSetChanged();
            }
        });

        if (!flag) {
            final View finalView1 = view;
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    if (r.getStatus() == Status.DELIVERY || r.getStatus() == Status.DELIVERED) {
                        builder.setTitle(context.getString(R.string.title_deliver));
                        String msg = v.getResources().getString(R.string.order) + ": " + r.getOrder_id() + "\n"
                                + v.getResources().getString(R.string.date) + ": " + r.getDate() + " "
                                + v.getResources().getString(R.string.time) + ": " + r.getTime() + "\n"
                                + v.getResources().getString(R.string.surname) + ": " + r.getSurname() + "\n"
                                + v.getResources().getString(R.string.address) + ": " + r.getAddress() + "\n"
                                + v.getResources().getString(R.string.phone) + ": " + r.getPhone() + "\n";
                        builder.setMessage(msg);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    } else {
                        //If Status is COOKING then the restaurant can start the delivery part
                        if (r.getStatus() == Status.COOKING) {
                            builder.setTitle(context.getString(R.string.title_deliver));

                            builder.setMessage(context.getString(R.string.msg_deliver));
                            builder.setPositiveButton(context.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    /**
                                     * GO FROM RESERVATION to MAPSFRAGMENT
                                     */
                                    ReservationFragmentDirections.ActionReservationIdToMapsFragmentId action =
                                            ReservationFragmentDirections
                                                    .actionReservationIdToMapsFragmentId("loggedID", r);

                                    action.setReservation(r);
                                    action.setLoggedId(loggedID);
                                    Navigation.findNavController(finalView1).navigate(action);

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
                            //Status = ACCEPTANCE => Restaurant can reject or accept the order
                            builder.setTitle(context.getString(R.string.title_confirm));

                            builder.setMessage(context.getString(R.string.msg_confirm));

                            builder.setPositiveButton(context.getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                // Change button text
                                DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference()
                                        .child("restaurants")
                                        .child(loggedID)
                                        .child("Menu");

                                //Transaction to avoid multiple updates of the quantity of the same dish
                                dbReference.runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                        if(mutableData.getChildrenCount() == 0)
                                            return Transaction.success(mutableData);
                                        int updated= 0;
                                        for(MutableData m : mutableData.getChildren()){
                            /*
                             * In m there are all plates of restaurant menu;
                             * in c.getDishes() there are all plates of reservation;
                             * so for each plate of menu (m) it searches if is contained in the reservation;
                             * whenever it is found, the quantity is checked and eventually updated in the menu.
                             * When al plates of reservation are found (counter: int updated),
                             * the reservation status and the button text are updated,
                             * then the scanning of menu foods is stopped (pruning).
                             */
                                            Reservation reservation = reservations.get(group_pos);
                                            String foodID= m.getKey();
                                            Integer quantity= Integer.parseInt(mutableData.child(foodID).child("Quantity").getValue().toString());
                                            for( Dish d : reservation.getDishes()){
                                                if(d.getID().equals(foodID)){
                                                    if(quantity - d.getQuantity()< 0 ){
                                                        Locale locale= Locale.getDefault();
                                                        String localeShort = locale.toString().substring(0, 2);
                                                        if(localeShort.equals("en"))
                                                            showToast("Not enough " + d.getName().toLowerCase() +" to accept this order");
                                                        else
                                                            showToast("Non hai abbastanza " + d.getName().toLowerCase() +" per accettare quest'ordine");
                                                        return Transaction.success(mutableData);
                                                    }
                                                    m.child("Quantity").setValue((quantity-d.getQuantity()));
                                                    Transaction.success(mutableData);
                                                    updated++;
                                                    /*
                                                     * pruning
                                                     */
                                                    if(updated==r.getNumberOfDishes()){
                                                        r.setStatus(Status.COOKING);
                                                        FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(r.getOrder_id()).child("status").child("en").setValue("Cooking");
                                                        FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(r.getOrder_id()).child("status").child("it").setValue("Preparazione");

                                                        FirebaseDatabase.getInstance().getReference("customers").child(r.getCustomerID()).child("reservations").child(r.getOrder_id()).child("status").child("en").setValue("Cooking");
                                                        FirebaseDatabase.getInstance().getReference("customers").child(r.getCustomerID()).child("reservations").child(r.getOrder_id()).child("status").child("it").setValue("Preparazione");

                                                        r.setButtonText(context.getString(R.string.title_deliver));
                                                        return Transaction.success(mutableData);
                                                    }
                                                }
                                            }
                                            //TODO update quantity in food of reservation (list of reservations -> dishes)
                                        }
                                        return Transaction.success(mutableData);
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                        Log.d("Fabio", "Transaction completed");
                                    }
                                });
                                notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton(context.getString(R.string.choice_reject), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    r.setStatus(Status.REJECTED);
                                    //Adding reservation to History
                                    DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("History");
                                    dbReference.child(r.getOrder_id()).child("customerID").setValue(r.getCustomerID());
                                    dbReference.child(r.getOrder_id()).child("date").setValue(r.getDate());
                                    dbReference.child(r.getOrder_id()).child("time").setValue(r.getTime());
                                    dbReference.child(r.getOrder_id()).child("totalPrice").setValue(r.getTotalPrice());
                                    dbReference.child(r.getOrder_id()).child("status/it").setValue("Rifiutato");
                                    dbReference.child(r.getOrder_id()).child("status/en").setValue("Rejected");
                                    dbReference.child(r.getOrder_id()).child("dishes").setValue(r.getDishes());

                                    //Delete reservation from pending reservations
                                    FirebaseDatabase.getInstance().getReference("restaurants/" + loggedID + "/reservations/" + r.getOrder_id()).removeValue();
                                    FirebaseDatabase.getInstance().getReference("customers").child(r.getCustomerID()).child("reservations").child(r.getOrder_id()).child("status").child("en").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference("customers").child(r.getCustomerID()).child("reservations").child(r.getOrder_id()).child("status").child("it").setValue("Rifiutato");
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
        final Reservation c= reservations.get(i);
        final ChildHolder holder;
        final int group_pos = i, child_pos =i1;

        if(view == null){
            holder= new ChildHolder();

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_child,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);
            holder.dish_chechbox= (CheckBox) view.findViewById(R.id.dish_checkbox);

            view.setTag(holder);
        }
        else {
            holder = ((ChildHolder)view.getTag());
//            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
//            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
//            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);
//            holder.dish_chechbox= (CheckBox) view.findViewById(R.id.dish_checkbox);
        }

        holder.dish_chechbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean state = childsChecked.get(c.getOrder_id()).get(child_pos);
                childsChecked.get(c.getOrder_id()).set(child_pos, state ? false : true);
                Log.d("Parent", "parent position: " + group_pos);
                Log.d("Child",  "child position: " + child_pos);
                if(state) { // se true, lo stato sta passando a false
                    if(groupCheckBoxes.get(c.getOrder_id()).isChecked()) {
                        groupCheckBoxes.get(c.getOrder_id()).setChecked(false);
                        groupChecked.put(c.getOrder_id(), false);
                    }
                }
            }
        });

        if(childsChecked.size() <= i){
            ArrayList<Boolean>childStates = new ArrayList<>();
            for(int j=0 ; j < listHashMap.get(c.getOrder_id()).size(); j++){
                if(childStates.size() > i1){
                    childStates.add(i1, false);
                }
                else
                    childStates.add(false);
                if(childsChecked.size() > group_pos){
                    childsChecked.put(c.getOrder_id(),childStates);
                }
                else
                    childsChecked.put(c.getOrder_id(),childStates);
            }
        }else{
            holder.dish_chechbox.setChecked(childsChecked.get(c.getOrder_id()).get(i1));
        }

        holder.tv_dish_name.setText(dish.getName());
        holder.tv_dish_quantity.setText(dish.getQuantity().toString());
        holder.tv_dish_notes.setText(dish.getNotes());



        if (c.getStatus() == Status.COOKING) {
            //Se lo stato della prenotazione è COOKING, rendo la checkbox visibile
            holder.dish_chechbox.setVisibility(View.VISIBLE);
        }
        else
            holder.dish_chechbox.setVisibility(View.GONE);


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

    public void showToast(final String text)
    {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        java.lang.Runnable runnableToast = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        };

        mainHandler.post(runnableToast);
    }

    private class ViewHolder {
        Button button;
        CheckBox selectAllCheckBox;
        TextView tv_date;
        TextView tv_time;
        TextView tv_status;
    }

    private class ChildHolder{
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;
        CheckBox dish_chechbox;

    }
}