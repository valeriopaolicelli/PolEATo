package com.mad.poleato.Reservation.RiderSelection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.DailyOffer.DailyOfferFragmentDirections;
import com.mad.poleato.R;
import com.mad.poleato.Reservation.Reservation;
import com.mad.poleato.Reservation.Status;
import com.mad.poleato.Rider;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class RiderListAdapter extends ArrayAdapter<Rider>
{
    private List<Rider> ridersList;
    private final Context mContext;
    private final LayoutInflater inflater;
    private final Reservation reservation;
    private final String loggedID;

    public RiderListAdapter(Context context, int resourceId, Reservation r, String loggedID)
    {
        super(context, resourceId);

        mContext = context;
        ridersList = new ArrayList<>();
        inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.reservation= r;
        this.loggedID = loggedID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final ViewHolder holder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.rider_layout, null);
            holder = new ViewHolder();
            holder.riderID_tv= (TextView) convertView.findViewById(R.id.rider_tv);
            holder.distance_tv= (TextView) convertView.findViewById(R.id.distance_tv);
            holder.selectButton= (Button) convertView.findViewById(R.id.myButton);
            holder.busy_tv= (TextView) convertView.findViewById(R.id.status_tv) ;
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)convertView.getTag();
        }

        final Rider item = getItem(position);
        if (item != null)
        {
            // Set up the views.
            holder.riderID_tv.setText(ridersList.get(position).getId());

            /*
             * setup the distance format:
             *  e.g 120.4 m or 1.3 Km
             */
            String distance_text;
            if(ridersList.get(position).getDistance() < 1)
                // distance consists in meters
                distance_text= new DecimalFormat("##.##").format(1000*ridersList.get(position).getDistance()) + " m";
            else
                // distance consists in kilometers
                distance_text= new DecimalFormat("##.##").format(ridersList.get(position).getDistance()) + " Km";
            holder.distance_tv.setText(distance_text);

            /*
             * Listener of button 'select rider'
             */
            final View finalConvertView = convertView;
            holder.selectButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getContext().getString(R.string.rider_selected) + ": " + holder.riderID_tv.getText().toString());

                    builder.setMessage(getContext().getString(R.string.msg_rider_selected));
                    builder.setPositiveButton(getContext().getString(R.string.choice_confirm), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final String riderID= item.getId();
                            reservation.setStat(getContext().getString(R.string.delivery));
                            reservation.setStatus(Status.DELIVERY);
                            FirebaseDatabase.getInstance().getReference("customers/"+reservation.getCustomerID()
                                    +"/reservations/"+reservation.getOrder_id()+"/status/en").setValue("Delivering");
                            FirebaseDatabase.getInstance().getReference("customers/"+reservation.getCustomerID()
                                    +"/reservations/"+reservation.getOrder_id()+"/status/it").setValue("In Consegna");
                            notifyRider(riderID, finalConvertView);
                        }
                    });
                    builder.setNegativeButton(getContext().getString(R.string.choice_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            String status= ridersList.get(position).getStatus();
            int numberOfOrder= ridersList.get(position).getNumberOfOrder();
            if(status.equals("Busy") || status.equals("Free")) {
                if(numberOfOrder == 1)
                    holder.busy_tv.setText(String.format("%s with %d pending order before your", status, numberOfOrder));
                else if (numberOfOrder > 1)
                    holder.busy_tv.setText(String.format("%s with %d pending orders before your", status, numberOfOrder));
                else
                    holder.busy_tv.setText(String.format("%s with no further pending order before your", status));
            }
            else if(status.equals("Occupato") || status.equals("Libero")){
                if(numberOfOrder == 1)
                    holder.busy_tv.setText(String.format("%s con %d ordine prima del tuo", status, numberOfOrder));
                else if (numberOfOrder > 1)
                    holder.busy_tv.setText(String.format("%s con %d ordini prima del tuo", status, numberOfOrder));
                else
                    holder.busy_tv.setText(String.format("%s senza altri ordine prima del tuo", status));
            }
        }

        return convertView;
    }

    @Override
    public int getCount()
    {
        return ridersList.size();
    }

    @Override
    public Rider getItem(int position)
    {
        return ridersList.get(position);
    }

    public void addRider(Rider rider){
        ridersList.add(rider);
        Collections.sort(ridersList, Rider.distanceComparator);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private void notifyRider(final String riderID, final View convertView) {

        /* retrieve the restaurant information */
        DatabaseReference referenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants").child(loggedID);
        referenceRestaurant.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshotRestaurant) {
                if( getContext() != null &&
                        dataSnapshotRestaurant.exists() &&
                        dataSnapshotRestaurant.hasChild("Address") &&
                        dataSnapshotRestaurant.hasChild("Name") &&
                        dataSnapshotRestaurant.hasChild("Phone") &&
                        dataSnapshotRestaurant.hasChild("reservations") &&
                        dataSnapshotRestaurant.child("reservations").hasChild(reservation.getOrder_id()) &&
                        dataSnapshotRestaurant.child("reservations/"+reservation.getOrder_id()).hasChild("status") &&
                        dataSnapshotRestaurant.child("reservations/"+reservation.getOrder_id()+"/status").hasChild("it") &&
                        dataSnapshotRestaurant.child("reservations/"+reservation.getOrder_id()+"/status").hasChild("en") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                                                 +"/status/it").getValue().toString().equals("Preparazione") &&
                        dataSnapshotRestaurant.child("reservations/" + reservation.getOrder_id()
                                                                 +"/status/en").getValue().toString().equals("Cooking")) {

                    DatabaseReference referenceRider = FirebaseDatabase.getInstance().getReference("deliveryman").child(riderID);
                    DatabaseReference reservationRider = referenceRider.child("requests").push();
                    final String addressRestaurant = dataSnapshotRestaurant.child("Address").getValue().toString();
                    final String nameRestaurant = dataSnapshotRestaurant.child("Name").getValue().toString();
                    final String phoneRestaurant = dataSnapshotRestaurant.child("Phone").getValue().toString();
                    reservationRider.child("addressCustomer").setValue(reservation.getAddress());
                    reservationRider.child("addressRestaurant").setValue(addressRestaurant);
                    reservationRider.child("CustomerID").setValue(reservation.getCustomerID());

                    //update the delivery status
                    reservationRider.child("nameRestaurant").setValue(nameRestaurant);
                    reservationRider.child("numberOfDishes").setValue(reservation.getNumberOfDishes());
                    reservationRider.child("orderID").setValue(reservation.getOrder_id());
                    reservationRider.child("restaurantID").setValue(loggedID);
                    reservationRider.child("nameCustomer").setValue(reservation.getName() + " " + reservation.getSurname());
                    reservationRider.child("totalPrice").setValue(reservation.getTotalPrice());
                    reservationRider.child("phoneCustomer").setValue(reservation.getPhone());
                    reservationRider.child("phoneRestaurant").setValue(phoneRestaurant);

                    // compose the date in the format YYYY/MM/DD HH:mm
                    String[] date_components = reservation.getDate().split("/"); //format: dd/mm/yyyy
                    String timeStr = date_components[2]+"/"+date_components[1]+"/"+date_components[0]+" "+
                            reservation.getTime();
                    reservationRider.child("deliveryTime").setValue(timeStr);

                    FirebaseDatabase.getInstance().getReference("restaurants/"
                                                +loggedID+"/reservations/"+reservation.getOrder_id()+"/status/en").setValue("Delivering");
                    FirebaseDatabase.getInstance().getReference("restaurants/"
                                                +loggedID+"/reservations/"+reservation.getOrder_id()+"/status/it").setValue("In consegna");
                    sendNotification(riderID);

                    /**
                     * GO FROM MAPSFRAGMENT to RESERVATION
                     */
                    Navigation.findNavController(convertView).navigate(R.id.action_mapsFragment_id_to_reservation_id);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Valerio", "NotifyRandomRider -> retrieve restaurant info: " + databaseError.getMessage());
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

                                + "\"data\": {\"Order\": \"PolEATo\"},"
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

    public void removeRider(String riderID) {
        for(int i=0; i < ridersList.size(); i++)
            if(ridersList.get(i).getId().equals(riderID)) {
                ridersList.get(i).getMarker().remove();
                ridersList.remove(i);
                Collections.sort(ridersList, Rider.distanceComparator);
            }
    }

    public class ViewHolder
    {
        TextView riderID_tv;
        TextView distance_tv;
        Button selectButton;
        TextView busy_tv;
    }
}
