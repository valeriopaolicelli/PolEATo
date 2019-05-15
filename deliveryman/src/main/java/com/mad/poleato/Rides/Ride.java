package com.mad.poleato.Rides;

import java.io.Serializable;

public class Ride implements Serializable {

    private String orderID;
    private String addressCustomer;
    private String addressRestaurant;
    private String nameCustomer;
    private String customerID;
    private String restaurantID;
    private String nameRestaurant;
    private String totalPrice;
    private String numberOfDishes;
    private String phoneCustomer;
    private String phoneRestaurant;
    private String time;


    public Ride(String orderID, String addressCustomer, String addressRestaurant,
                String nameCustomer, String nameRestaurant, String totalPrice,
                String numberOfDishes, String phoneCustomer, String phoneRestaurant,
                String time, String customerID, String restaurantID) {
        this.orderID = orderID;
        this.nameCustomer = nameCustomer;
        this.nameRestaurant = nameRestaurant;
        this.addressCustomer = addressCustomer;
        this.addressRestaurant = addressRestaurant;
        this.nameCustomer = nameCustomer;
        this.totalPrice = totalPrice;
        this.numberOfDishes = numberOfDishes;
        this.phoneCustomer = phoneCustomer;
        this.phoneRestaurant = phoneRestaurant;
        this.restaurantID= restaurantID;
        this.customerID= customerID;
        this.time = time;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public String getOrderID() {
        return orderID;
    }

    public String getAddressCustomer() {
        return addressCustomer;
    }

    public String getAddressRestaurant() {
        return addressRestaurant;
    }

    public String getNameCustomer() {
        return nameCustomer;
    }

    public String getNameRestaurant() {
        return nameRestaurant;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getNumberOfDishes() {
        return numberOfDishes;
    }

    public String getPhoneCustomer() {
        return phoneCustomer;
    }

    public String getPhoneRestaurant() {
        return phoneRestaurant;
    }

    public String getTime() {
        return time;
    }

}
