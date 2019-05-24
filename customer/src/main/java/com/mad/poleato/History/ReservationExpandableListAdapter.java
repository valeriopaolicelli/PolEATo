package com.mad.poleato.History;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.Classes.Dish;
import com.mad.poleato.R;
import com.mad.poleato.Classes.Reservation;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class ReservationExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<Reservation> reservations;
    private HashMap<String, List<Dish>>listHashMap;
    private String loggedID;

    public ReservationExpandableListAdapter(Context context, List<Reservation> reservations, HashMap<String, List<Dish>> listHashMap, String currentUserID) {
        this.context = context;
        this.reservations = reservations;
        this.listHashMap = listHashMap;
        this.loggedID= currentUserID;
    }


    @Override
    public int getGroupCount() {
        return reservations.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return listHashMap.get(reservations.get(i).getOrderID()).size();
    }

    @Override
    public Object getGroup(int i) {
        return reservations.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return listHashMap.get(reservations.get(i).getOrderID()).get(i1); //i = group item, i1 = child item
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
        if( view ==  null){
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_group,null);

            holder.tv_date = (TextView)view.findViewById(R.id.tvDateField);
            holder.tv_time = (TextView) view.findViewById(R.id.tvTimeField);
            holder.tv_restaurant_name= (TextView)view.findViewById(R.id.tvRestaurantField);
            holder.tv_total_price= (TextView) view.findViewById(R.id.tvTotalPrice);
            holder.tv_review = (TextView) view.findViewById(R.id.reviewtv);
            holder.tv_status = (TextView) view.findViewById(R.id.statusTv);
            holder.confirm_button = (Button) view.findViewById(R.id.confirmBtn);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        String price= c.getTotalPrice() + "€";


        if(c.getStatus().equals("Delivered") || c.getStatus().equals("Consegnato")){
            // The order has been delivered
            holder.tv_review.setVisibility(View.VISIBLE);
            holder.confirm_button.setVisibility(View.GONE);
            holder.tv_status.setText(c.getStatus());
            holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
            holder.tv_status.setPaintFlags(holder.tv_review.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            //Check if this order has been reviewed
            if (c.isReviewFlag()) {
                holder.tv_review.setText(context.getResources().getString(R.string.review_history_done));
                holder.tv_review.setClickable(false);
                holder.tv_review.setTextColor(Color.GREEN);
                holder.tv_review.setPaintFlags(holder.tv_review.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            } else {
                holder.tv_review.setText(context.getResources().getString(R.string.review_history_request));
                holder.tv_review.setClickable(true);
                holder.tv_review.setPaintFlags(holder.tv_review.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                holder.tv_review.setTextColor(context.getResources().getColor(R.color.colorTextField));
                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
                holder.tv_review.setBackgroundResource(outValue.resourceId);
                holder.tv_review.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("restaurantID", c.getRestaurantID());
                        bundle.putString("restaurantName", c.getRestaurantName());
                        bundle.putString("orderID", c.getOrderID());

                        Navigation.findNavController(view).navigate(R.id.action_holder_history_id_to_ratingFragment, bundle);
                        //Toast.makeText(context, "Review Text Clicked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        else {
            //The order has still not been delivered, customer has to confirm the order arrival
            holder.tv_status.setPaintFlags(holder.tv_review.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
            if (c.getStatus().equals("Delivering") || c.getStatus().equals("In Consegna")) {
                holder.tv_review.setVisibility(View.GONE);
                holder.confirm_button.setVisibility(View.VISIBLE);
                holder.tv_status.setText(c.getStatus());
                holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextAccepted));
                holder.confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final View viewClicked = view;
                        final AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle(context.getResources().getString(R.string.title_confirm_dialog))
                                .setMessage(context.getResources().getString(R.string.message_confirm_dialog))
                                .setPositiveButton(context.getResources().getString(R.string.confirm_btn), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        //Upload status for restaurant
                                        FirebaseDatabase.getInstance()
                                                .getReference("restaurants/" + c.getRestaurantID() + "/reservations/" + c.getOrderID()
                                                        + "/status/en").setValue("Delivered");

                                        FirebaseDatabase.getInstance()
                                                .getReference("restaurants/" + c.getRestaurantID() + "/reservations/" + c.getOrderID()
                                                        + "/status/it").setValue("Consegnato");
                                        c.setStatus("Delivered");

                                        //Upload status for customer
                                        FirebaseDatabase.getInstance()
                                                .getReference("customers/" + loggedID + "/reservations/" + c.getOrderID()
                                                        + "/status/en").setValue("Delivered");
                                        FirebaseDatabase.getInstance()
                                                .getReference("customers/" + loggedID + "/reservations/" + c.getOrderID()
                                                        + "/status/it").setValue("Consegnato");

                                        sendNotification(c.getRestaurantID(), c.getOrderID());

                                        notifyDataSetChanged();

                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context)
                                                .setTitle(context.getResources().getString(R.string.title_review_dialog))
                                                .setMessage(context.getResources().getString(R.string.message_review))
                                                .setPositiveButton(context.getResources().getString(R.string.review_history_request), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        Bundle bundle = new Bundle();
                                                        bundle.putString("restaurantID", c.getRestaurantID());
                                                        bundle.putString("restaurantName", c.getRestaurantName());
                                                        bundle.putString("orderID", c.getOrderID());

                                                        Navigation.findNavController(viewClicked).navigate(R.id.action_holder_history_id_to_ratingFragment, bundle);
                                                        dialogInterface.dismiss();
                                                    }
                                                }).setNeutralButton(context.getResources().getString(R.string.notnowBtn), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                        AlertDialog dialog1 = builder1.create();
                                        dialog1.show();
                                    }
                                })
                                .setNeutralButton(context.getResources().getString(R.string.cancelBTn), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            }else{
                if (c.getStatus().equals("New Order") || c.getStatus().equals("Nuovo Ordine")) {
                    holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
                    holder.tv_status.setText(c.getStatus());
                } else if (c.getStatus().equals("Cooking") || c.getStatus().equals("Preparazione")) {
                    holder.tv_status.setTextColor(context.getResources().getColor(R.color.colorTextSubField));
                    holder.tv_status.setText(c.getStatus());
                }
                holder.confirm_button.setVisibility(View.GONE);
                holder.tv_review.setVisibility(View.GONE);
            }
        }
        holder.tv_date.setText(c.getDate());
        holder.tv_time.setText(c.getTime());
        holder.tv_total_price.setText(price);
        holder.tv_restaurant_name.setText(c.getRestaurantName());
        notifyDataSetChanged();
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean isLast, View view, final ViewGroup viewGroup) {

        final Dish dish = (Dish) getChild(i,i1);
        final Reservation c= reservations.get(i);
        final ChildHolder holder;

        if(view == null){
            holder= new ChildHolder();

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_reservation_child,null);
            holder.tv_dish_name= (TextView) view.findViewById(R.id.tv_dish_name);
            holder.tv_dish_quantity = (TextView) view.findViewById(R.id.tv_dish_quantity);
            holder.tv_dish_notes= (TextView) view.findViewById(R.id.tv_dish_note);

            view.setTag(holder);
        }
        else {
            holder = ((ChildHolder)view.getTag());
        }

        holder.tv_dish_name.setText(dish.getName());
        holder.tv_dish_quantity.setText(dish.getQuantity().toString());
        holder.tv_dish_notes.setText(dish.getNotes());
        notifyDataSetChanged();
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

    public void updateReservations(List<Reservation> reservations) {
        this.reservations=reservations;
    }

    private void sendNotification(final String restaurantID, final String orderID) {
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
                    send_email= restaurantID;

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

                                + "\"data\": {\"Order\": \"PolEATo\"},"
                                + "\"contents\": {\"en\": \"Order " + orderID + " paid\"}"
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


    private class ViewHolder {
        TextView tv_date;
        TextView tv_time;
        TextView tv_restaurant_name;
        TextView tv_total_price;
        TextView tv_review;
        TextView tv_status;
        Button confirm_button;
    }

    private class ChildHolder{
        TextView tv_dish_name;
        TextView tv_dish_quantity;
        TextView tv_dish_notes;

    }
}