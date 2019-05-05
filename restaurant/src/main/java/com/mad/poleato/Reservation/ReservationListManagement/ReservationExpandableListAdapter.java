package com.mad.poleato.Reservation.ReservationListManagement;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.Dish;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.Status;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


import static android.view.View.GONE;


public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>>listHashMap;
    private ArrayList<Boolean>groupChecked = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, ArrayList<Boolean>>childsChecked = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, CheckBox> groupCheckBoxes = new HashMap<>();
    private String loggedID;
    boolean notify;

    public ReservationExpandableListAdapter(Context context, List<Reservation> reservations, HashMap<String, List<Dish>> listHashMap, String currentUserID) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
        this.loggedID= currentUserID;

        // flag to decide if notify the rider
        notify = false;
        //initialize default check states of checkboxes
        initCheckStates(false);
    }

    /**
     * Called to initialize the default check states of items
     * @param defaultState : false
     */
    private void initCheckStates(boolean defaultState) {
        for(int i = 0 ; i <reservations.size(); i++){
            groupChecked.add(i, defaultState);
            Reservation r = reservations.get(i);
            ArrayList<Boolean> childStates = new ArrayList<>();
            for(int j = 0; j < listHashMap.get(r.getOrder_id()).size(); j++){
                childStates.add(defaultState);
            }

            childsChecked.put(i, childStates);
        }
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
        final List<Dish> dishes = c.getDishes();
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
            groupCheckBoxes.put(i, holder.selectAllCheckBox);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        holder.tv_date.setText(c.getDate());
        holder.tv_time.setText(c.getTime());
        holder.tv_status.setText(c.getStat());

        if(groupChecked.size()<=i){
            groupChecked.add(i,false);
        }else{
            holder.selectAllCheckBox.setChecked(groupChecked.get(i));
            groupCheckBoxes.put(i, holder.selectAllCheckBox);

        }


        if (c.getStatus() == Status.REJECTED) {
            flag = true;
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextRejected));
        }
        else if (c.getStatus() == Status.DELIVERY) {
            holder.button.setText(context.getResources().getString(R.string.order_info));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
            holder.button.setVisibility(View.VISIBLE);
        }
        else if (c.getStatus() == Status.ACCEPTANCE ) {
            holder.button.setText(context.getResources().getString(R.string.button_reservation));
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
            holder.button.setVisibility(View.VISIBLE);
        }
        // Se lo stato è COOKING allora compare la checkbox
        if(c.getStatus() == Status.COOKING) {
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
                boolean state = groupChecked.get(group_pos);
                groupChecked.set(group_pos, state ? false : true);
                groupCheckBoxes.get(group_pos).setChecked(state ? false : true);
                ArrayList<Boolean>childs = childsChecked.get(group_pos);
                for ( int i=0 ; i<listHashMap.get(c.getOrder_id()).size(); i++){
                    childs.set(i, state ? false : true);
                }
                childsChecked.put(group_pos,childs);
                Log.d("GroupCheckbox", "Clicked Group checkbox: "+ group_pos);
                notifyDataSetChanged();
            }
        });

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
                                + v.getResources().getString(R.string.address) + ": " + c.getAddress() + "\n"
                                + v.getResources().getString(R.string.phone) + ": " + c.getPhone() + "\n";
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

                                //TODO when log in will be enabled, change the R00 child, with the proper one
                                FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("en").setValue("Delivering");
                                FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("it").setValue("In consegna");
                                c.setStatus(Status.DELIVERY, context);
                                holder.button.setText(context.getString(R.string.order_info));
                                c.setButtonText(context.getString(R.string.order_info));
                                notify = true;
                                notifyRandomRider(c);
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
                                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("en").setValue("Cooking");
                                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("it").setValue("Preparazione");
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
                                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("en").setValue("Rejected");
                                    FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID).child("reservations").child(c.getOrder_id()).child("status").child("it").setValue("Rifiutato");
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

    private void notifyRandomRider(final Reservation c) {
        final DatabaseReference referenceRider= FirebaseDatabase.getInstance().getReference("deliveryman");
        final long[] numberOfRider = new long[1];
        final List<String> riderIDs= new ArrayList<>();
        referenceRider.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshotRider) {
                numberOfRider[0] = dataSnapshotRider.getChildrenCount();
                if (riderIDs.size() == 0) {
                    for (DataSnapshot ds : dataSnapshotRider.getChildren())
                        riderIDs.add(ds.getKey());

                    /* retrieve the restaurant information */
                    final String[] addressRestaurant = new String[1];
                    final String[] nameRestaurant = new String[1];
                    DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID);
                    referenceRestaurant.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshotRestaurant) {
                            /* random selection of rider */
                            int selectedRider = (int) (Math.random() * numberOfRider[0]);
                            //TODO retrieve real user id of rider from db
                            String childID = riderIDs.get(selectedRider);
                            DatabaseReference reservationRider = referenceRider.child(childID).child("reservations").push();

                            if (dataSnapshotRestaurant.exists() &&
                                    dataSnapshotRestaurant.hasChild("Address") &&
                                    dataSnapshotRestaurant.hasChild("Name")) {

                                addressRestaurant[0] = dataSnapshotRestaurant.child("Address").getValue().toString();
                                nameRestaurant[0] = dataSnapshotRestaurant.child("Name").getValue().toString();
                                if (notify) {
                                    reservationRider.child("surnameCustomer").setValue(c.getSurname());
                                    reservationRider.child("addressCustomer").setValue(c.getAddress());
                                    reservationRider.child("orderID").setValue(c.getOrder_id());
                                    reservationRider.child("numberOfDishes").setValue(c.getNumberOfDishes());
                                    reservationRider.child("totalPrice").setValue(c.getTotalPrice());
                                    reservationRider.child("addressRestaurant").setValue(addressRestaurant[0]);
                                    reservationRider.child("nameRestaurant").setValue(nameRestaurant[0]);
                                    sendNotification(childID);
                                    notify = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.d("Valerio", "NotifyRandomRider -> retrieve restaurant info: " + databaseError.getMessage());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "NotifyRandomRider -> retrieve number of deliverymans: " + databaseError.getMessage());
            }
        });
    }

    private void sendNotification(final String childID) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int SDK_INT = android.os.Build.VERSION.SDK_INT;
                if (SDK_INT > 8) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String send_email;

                    //This is a Simple Logic to Send Notification different Device Programmatically....
                    send_email= childID;

                    try {
                        String jsonResponse;

                        URL url = new URL("https://onesignal.com/api/v1/notifications");
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);

                        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        con.setRequestProperty("Authorization", "Basic YjdkNzQzZWQtYTlkYy00MmIzLTg0NDUtZmQ3MDg0ODc4YmQ1");
                        con.setRequestMethod("POST");

                        String strJsonBody = "{"
                                + "\"app_id\": \"a2d0eb0d-4b93-4b96-853e-dcfe6c34778e\","

                                + "\"filters\": [{\"field\": \"tag\", \"key\": \"User_ID\", \"relation\": \"=\", \"value\": \"" + send_email + "\"}],"

                                + "\"data\": {\"Delivery\": \"New order\"},"
                                + "\"contents\": {\"en\": \"New order to deliver\"}"
                                + "}";


                        System.out.println("strJsonBody:\n" + strJsonBody);

                        byte[] sendBytes = strJsonBody.getBytes("UTF-8");
                        con.setFixedLengthStreamingMode(sendBytes.length);

                        OutputStream outputStream = con.getOutputStream();
                        outputStream.write(sendBytes);

                        int httpResponse = con.getResponseCode();
                        System.out.println("httpResponse: " + httpResponse);

                        if (httpResponse >= HttpURLConnection.HTTP_OK
                                && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                            Scanner scanner = new Scanner(con.getInputStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        } else {
                            Scanner scanner = new Scanner(con.getErrorStream(), "UTF-8");
                            jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                            scanner.close();
                        }
                        System.out.println("jsonResponse:\n" + jsonResponse);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });
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
                boolean state = childsChecked.get(group_pos).get(child_pos);
                childsChecked.get(group_pos).set(child_pos, state ? false : true);
                Log.d("Parent", "parent position: " + group_pos);
                Log.d("Child",  "child position: " + child_pos);
                if(state) { // se true, lo stato sta passando a false
                    if(groupCheckBoxes.get(group_pos).isChecked()) {
                        groupCheckBoxes.get(group_pos).setChecked(false);
                        groupChecked.set(group_pos, false);
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
                    childsChecked.put(group_pos,childStates);
                }
                else
                    childsChecked.put(group_pos,childStates);
            }
        }else{
            holder.dish_chechbox.setChecked(childsChecked.get(i).get(i1));
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


    public void addReservation(Reservation r){
        this.listHashMap.put(r.getOrder_id(), r.getDishes());
        this.reservations.add(r);

        this.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
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