package com.mad.poleato.Rides;

public class Ride {

    private String orderID;
    private String surname;
    private String deliveryAddress;
    private String restaurantName;
    private String restaurantAddress;
    private Double totalPrice;
    private Integer totalDishes;

    public Ride(String orderID, String surname, String deliveryAddress, String restaurantName, String restaurantAddress, Double totalPrice, Integer totalDishes) {
        this.orderID = orderID;
        this.surname = surname;
        this.deliveryAddress = deliveryAddress;
        this.restaurantName = restaurantName;
        this.restaurantAddress = restaurantAddress;
        this.totalPrice = totalPrice;
        this.totalDishes = totalDishes;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Integer getTotalDishes() {
        return totalDishes;
    }

    public void setTotalDishes(Integer totalDishes) {
        this.totalDishes = totalDishes;
    }
}
