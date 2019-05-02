package com.mad.poleato;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
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

    public void uploadOrder() {
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("restaurants");
        DatabaseReference reservation =  dbReference.child(this.getRestaurantID()).child("reservations").push();
        reservation.setValue(new Order(this.status,this.customerID,this.totalPrice,this.date,this.time));
        reservation.child("dishes").setValue(this.getDishes());
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
