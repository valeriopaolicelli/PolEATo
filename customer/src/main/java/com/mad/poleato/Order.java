package com.mad.poleato;

import com.google.firebase.database.Exclude;

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

    /**
     * The Annotation @Exclude must be keep to stop the FireBase interpreter that in new version of Android
     * seems to returns arrays (which are not Serializable) instead of Lists
     * @return List of food
     */
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
}
