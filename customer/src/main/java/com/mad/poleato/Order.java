package com.mad.poleato;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

   private List<Food> selectedFoods;
   private Double totalPrice;
   private String customerID; //TODO: must be implemented with login phase
   private String date;
   private String status;
   private String restaurantID;
   private Restaurant r;
   private String time;

    public Order() {
        this.totalPrice=0.0;
        selectedFoods=new ArrayList<>();
        status = "New Order";
        customerID = "C00"; // TODO; Must be restrieved from database
    }

    public void updateTotalPrice(){
       totalPrice = 0.0;
       if(!selectedFoods.isEmpty()){
         for(Food f:selectedFoods){
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
        this.selectedFoods.add(f);
    }
    public void removeFoodFromOrder(Food f){
        this.selectedFoods.remove(f);
    }

    @Exclude
    public List<Food> getSelectedFoods(){
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void uploadOrder() {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("restaurants");
        DatabaseReference reservation =  dbReference.child(this.getRestaurantID()).child("reservations").push();
        reservation.setValue(this);
        String dbkey = reservation.getKey();
        dbReference.child(this.getRestaurantID()).child("reservations").child(dbkey).child("dishes").setValue(this.getSelectedFoods());
        dbReference.child(this.getRestaurantID()).child("reservations").child(dbkey).child("date").setValue(this.getDate());
        dbReference.child(this.getRestaurantID()).child("reservations").child(dbkey).child("time").setValue(this.getTime());
        dbReference.child(this.getRestaurantID()).child("reservations").child(dbkey).child("status").child("it").setValue("Nuovo ordine");
        dbReference.child(this.getRestaurantID()).child("reservations").child(dbkey).child("status").child("en").setValue("New order");
    }

}
