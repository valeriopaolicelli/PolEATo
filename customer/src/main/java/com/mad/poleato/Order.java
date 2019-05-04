package com.mad.poleato;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Order implements Serializable {

   private HashMap<String,Food> selectedFoods;
   private List<Food> dishes;
   private Double totalPrice;
   private String customerID; //TODO: must be implemented with login phase
   private String date;
   private String status;
   private String restaurantID;
   private Restaurant r;
   private String time;

    public Order() {
        this.totalPrice=0.0;
        selectedFoods=new HashMap<>();
        status = "New Order";
        customerID = "C00"; // TODO; Must be retrieved from database
    }
    public Order(String status, String customerID, Double totalPrice, String date, String time){
        this.status=status;
        this.customerID=customerID;
        this.totalPrice=totalPrice;
        this.date=date;
        this.time=time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void updateTotalPrice(){
       totalPrice = 0.0;
       if(!selectedFoods.isEmpty()){
         for(Food f:selectedFoods.values()){
           totalPrice+=f.getPrice()*f.getSelectedQuantity();
        }
       totalPrice += r.getDeliveryCost();
       }
   }

    public String getCustomerID() {
        return customerID;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void addFoodToOrder(Food f){
        this.selectedFoods.put(f.getName(),f);
    }
    public void removeFoodFromOrder(Food f){
        this.selectedFoods.remove(f.getName());
    }

    @Exclude
    public HashMap<String,Food> getSelectedFoods(){
        return selectedFoods;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public void setR(Restaurant r) {
        this.r = r;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public void uploadOrder() {
        /*
         * UPLOAD order into restaurant table
         */

        DatabaseReference dbReferenceRestaurant = FirebaseDatabase.getInstance().getReference("restaurants");
        DatabaseReference reservationRestaurant =  dbReferenceRestaurant.child(this.getRestaurantID()).child("reservations").push();

        Order o = new Order();

        o.setDate(this.date);
        o.setTime(this.time);
        o.setTotalPrice(this.totalPrice);
        o.setCustomerID(this.customerID);
        o.setRestaurantID(this.restaurantID);

        reservationRestaurant.setValue(o);

        reservationRestaurant.child("status").child("it").setValue("Nuovo ordine");
        reservationRestaurant.child("status").child("en").setValue("New order");
        reservationRestaurant.child("dishes").setValue(this.getDishes());

        /*
         * Add order to customer order history
         */
        final String[] restaurantName = new String[1];
        final String orderID= reservationRestaurant.getKey();
        final List<Food> dishes= this.dishes;
        final String date= this.date;
        final String time= this.time;
        final Double totalPrice= this.totalPrice;
        final String customerID= this.customerID;

        DatabaseReference referenceRestaurantOfReservation = dbReferenceRestaurant.child(this.getRestaurantID());
        referenceRestaurantOfReservation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    restaurantName[0] = dataSnapshot.child("Name").getValue().toString();
                    List<Dish> dish= new ArrayList<>();
                    String name, notes;
                    int quantity;
                    for(Food f : dishes){
                        name= f.getName();
                        quantity= f.getSelectedQuantity();
                        notes= f.getCustomerNotes();
                        dish.add(new Dish(name, quantity, notes));
                    }
                    DecimalFormat df = new DecimalFormat("#.00");
                    String price = df.format(totalPrice);

                    Reservation reservation= new Reservation(orderID, restaurantName[0], date, time, price);
                    DatabaseReference referenceCustomer= FirebaseDatabase.getInstance().getReference("customers").child(customerID);
                    referenceCustomer.child("reservations").child(orderID).setValue(reservation);
                    referenceCustomer.child("reservations").child(orderID).child("dishes").setValue(dish);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Exclude
    public List<Food> getDishes() {
        return dishes;
    }

    @Exclude
    public void setDishes() {
        dishes = new ArrayList<>(this.selectedFoods.values());
    }
}
