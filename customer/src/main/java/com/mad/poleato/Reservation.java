package com.mad.poleato;

import android.content.Context;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;


public class Reservation {
    private String orderID;
    private String restaurantName;
    private String date;
    private String time;
    private String totalPrice;
    private List<Dish> dishes;

    public Reservation(String orderID, String restaurantName, String date, String time,
                       String totalPrice) {
        this.orderID = orderID;
        this.restaurantName = restaurantName;
        this.date = date;
        this.time= time;
        this.totalPrice= totalPrice;
    }
    public String getName() {
        return restaurantName;
    }

    public void setName(String name) {
        this.restaurantName = name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getOrderID() {
        return orderID;
    }

    @Exclude
    public List<Dish> getDishes() {
        return dishes;
    }

    @Exclude
    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getRestaurantName() {
        return restaurantName;
    }
}
