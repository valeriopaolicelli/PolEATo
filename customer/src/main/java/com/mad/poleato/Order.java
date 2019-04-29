package com.mad.poleato;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

   private List<Food> selectedFoods;
   private Double totalPrice;
   private String customerID; //TODO: must be implemented with login phase
   private String notes;
   private Date date;
   private String restaurantID;
   private Restaurant r;

    public Order() {
        this.totalPrice=0.0;
        selectedFoods=new ArrayList<>();
        
    }

    public void updateTotalPrice(){
       totalPrice = 0.0;
       for(Food f:selectedFoods){
           totalPrice+=f.getPrice();
       }
       totalPrice += r.getDeliveryCost();
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
}
