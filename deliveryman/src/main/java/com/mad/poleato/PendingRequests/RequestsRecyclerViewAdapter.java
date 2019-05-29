package com.mad.poleato.PendingRequests;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.R;
import com.mad.poleato.Ride.Ride;
import com.mad.poleato.Ride.RideComparator;
import com.mad.poleato.Ride.RideStatus;
import com.mad.poleato.Ride.ShowMoreFragment;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


public class RequestsRecyclerViewAdapter extends RecyclerView.Adapter<RequestsRecyclerViewAdapter.RideViewHolder>{

    private SortedSet<Ride> rideSet; //SortedSet sorted on date to generate sorted list
    private List<Ride> rideList;
    private Context context;
    private DatabaseReference rideReference;
    private DatabaseReference requestsReference;
    private Boolean busy;
    private Boolean isActive;
    private String currentUserID;

    private Toast myToast;


    public class RideViewHolder extends RecyclerView.ViewHolder{

        public TextView deliveryAddress_tv, customerName_tv, restaurant_tv,
                        phone_tv, dishes_tv, time_tv, cost_tv;
        public Button confirm_button;
        public ImageButton showMore_button;
        public View itemView;


        public RideViewHolder(@NonNull View itemView) {
            super(itemView);

            this.itemView = itemView;
            this.deliveryAddress_tv = (TextView) itemView.findViewById(R.id.deliveryAddress_tv);
            this.customerName_tv = (TextView) itemView.findViewById(R.id.customerName_tv);
            this.restaurant_tv = (TextView) itemView.findViewById(R.id.restaurant_tv);
            this.phone_tv = (TextView) itemView.findViewById(R.id.phone_tv);
            this.dishes_tv = (TextView) itemView.findViewById(R.id.dishes_tv);
            this.time_tv = (TextView) itemView.findViewById(R.id.time_tv);
            this.cost_tv = (TextView) itemView.findViewById(R.id.cost_tv);
            this.confirm_button = (Button) itemView.findViewById(R.id.confirm_button);
            this.showMore_button = (ImageButton) itemView.findViewById(R.id.showMoreButton);
        }
    }


    public RequestsRecyclerViewAdapter(Context context, String currentUserID, Toast t){
        this.myToast = t;
        this.rideSet = new TreeSet<>(new RideComparator());
        this.rideList = new ArrayList<>();
        this.context = context;
        this.currentUserID= currentUserID;
        rideReference = FirebaseDatabase.getInstance().getReference("deliveryman").child(currentUserID)
                                                            .child("ride");
        requestsReference = FirebaseDatabase.getInstance().getReference("deliveryman").child(currentUserID)
                                                            .child("requests");

        //wait for the listener to initialize it to the correct value
        busy = null;
        isActive = null;
    }


    public void addRequest(Ride ride){

        this.rideSet.add(ride);
        this.rideList = new ArrayList<>(rideSet);
        notifyDataSetChanged();
    }

    public void removeRequest(Ride ride){

        this.rideSet.remove(ride);
        this.rideList = new ArrayList<>(rideSet);
        notifyDataSetChanged();
    }


    public void setBusy(Boolean busy){
        this.busy = busy;
    }

    public void setIsActive(Boolean isActive){
        this.isActive= isActive;
    }


    @NonNull
    @Override
    public RequestsRecyclerViewAdapter.RideViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View listItem = layoutInflater.inflate(R.layout.request_layout, viewGroup, false);
        final RideViewHolder viewHolder = new RideViewHolder(listItem);

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final RequestsRecyclerViewAdapter.RideViewHolder rideViewHolder, final int position) {

        final Ride ride = rideList.get(position);
        rideViewHolder.deliveryAddress_tv.setText(ride.getAddressCustomer());
        rideViewHolder.customerName_tv.setText(ride.getNameCustomer());
        rideViewHolder.restaurant_tv.setText(ride.getNameRestaurant());
        rideViewHolder.phone_tv.setText(ride.getPhoneCustomer());
        rideViewHolder.dishes_tv.setText(ride.getNumberOfDishes());
        rideViewHolder.time_tv.setText(ride.getDeliveryTime().split(" ")[1]); //print only the hour and not the day

        //correctly format the price string
        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
        String s = ride.getTotalPrice().replace(",", ".");
        double d = Double.parseDouble(s);
        String priceStr = decimalFormat.format(d) + " €";
        rideViewHolder.cost_tv.setText(priceStr);

        if(ride.getDelivering()){
            setButtonGrey(rideViewHolder.confirm_button);
        }
        else{
            //set confirmation button listener
            rideViewHolder.confirm_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(busy != null && !busy){

                        if(isActive != null){
                            if(!isActive){
                                new AlertDialog.Builder(v.getContext())
                                    .setTitle(context.getString(R.string.alert_is_active))
                                    .setMessage(context.getString(R.string.turn_on_status))
                                    .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            FirebaseDatabase.getInstance().getReference("deliveryman/"
                                                                                    +currentUserID+"/IsActive").setValue(true);
                                            isActive= true;
                                            confirm_ride(ride, rideViewHolder);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.no), null)
                                    .show();
                            }
                        }

                        if(isActive != null && isActive) {
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle(context.getString(R.string.ride_start_alert))
                                    .setMessage(context.getString(R.string.ride_start_confirm))
                                    .setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            confirm_ride(ride, rideViewHolder);
                                        }
                                    })
                                    .setNegativeButton(context.getString(R.string.no), null)
                                    .show();
                        }

                    }
                    else {
                        myToast.setText(context.getString(R.string.already_busy));
                        myToast.show();
                    }

                } //OnClick end
            });
        }

        //set showMore button listener
        rideViewHolder.showMore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                //pass the restaurant info
                bundle.putString("name", ride.getNameRestaurant());
                bundle.putString("address", ride.getAddressRestaurant());
                bundle.putString("phone", ride.getPhoneRestaurant());

                ShowMoreFragment showmore = new ShowMoreFragment();
                showmore.setArguments(bundle);
                showmore.show(((AppCompatActivity)context).getSupportFragmentManager(), "show_more");
            }
        });

    }


    private void confirm_ride(Ride ride, RideViewHolder rideViewHolder){

        //set this ride to delivering
        requestsReference.child(ride.getOriginalRequestKey()).child("delivering").setValue(true);
        ride.setDelivering(true);


        rideReference.child("CustomerID").setValue(ride.getCustomerID());
        rideReference.child("addressCustomer").setValue(ride.getAddressCustomer());
        rideReference.child("addressRestaurant").setValue(ride.getAddressRestaurant());
        rideReference.child("deliveryTime").setValue(ride.getDeliveryTime());
        rideReference.child("nameCustomer").setValue(ride.getNameCustomer());
        rideReference.child("nameRestaurant").setValue(ride.getNameRestaurant());
        rideReference.child("numberOfDishes").setValue(ride.getNumberOfDishes());
        rideReference.child("orderID").setValue(ride.getOrderID());
        rideReference.child("phoneCustomer").setValue(ride.getPhoneCustomer());
        rideReference.child("phoneRestaurant").setValue(ride.getPhoneRestaurant());
        rideReference.child("restaurantID").setValue(ride.getRestaurantID());

        //set current time as start time for the ride
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String curr_time = dateFormat.format(new Date());
        rideReference.child("startTime").setValue(curr_time);

        //init the status for the order
        rideReference.child("status").setValue(RideStatus.TO_RESTAURANT.name());
        rideReference.child("totalPrice").setValue(ride.getTotalPrice());

        //set to busy both locally and online
        rideReference.getParent().child("Busy").setValue(true);
        busy = true;

        //upload the key for this request
        rideReference.child("requestKey").setValue(ride.getOriginalRequestKey());
        //make button disappear
        //v.setVisibility(View.GONE);
        setButtonGrey(rideViewHolder.confirm_button);

        /**
         * GO FROM PENDING REQUESTS TO CURRENT RIDE
         */
        Navigation.findNavController(rideViewHolder.itemView).navigate(R.id.action_pendingReservations_id_to_ride_id);
    }


    private void setButtonGrey(Button b){

        b.setText(context.getString(R.string.delivering));
        b.setBackground(context.getDrawable(R.drawable.ripple_black));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myToast.setText(context.getString(R.string.already_delivering));
                myToast.show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return rideList.size();
    }

}
