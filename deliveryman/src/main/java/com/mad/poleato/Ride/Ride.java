package com.mad.poleato.Ride;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class represents a single ride object
 */
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
    private String deliveryTime;
    private String startTime; //time at which the rider accepts the request
    private double km;
    private RideStatus status;

    private Boolean delivering; //this value is used only at request time

    //this is the key of the the request that generated this ride. It is used to remove the requests from the "requests" table
    private String originalRequestKey;
    public static Comparator<Ride> deliveryTimeInverseComparator;


    public Ride(String orderID, String addressCustomer, String addressRestaurant,
                String nameCustomer, String nameRestaurant, String totalPrice,
                String numberOfDishes, String phoneCustomer, String phoneRestaurant,
                String deliveryTime, String customerID, String restaurantID,
                String startTime, RideStatus status, String requestKey, Boolean delivering) {
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
        this.deliveryTime = deliveryTime;
        this.startTime = startTime;
        this.km = 0.0; //the first time this value is set to 0 and the it will update with the setter

        this.originalRequestKey = requestKey;
        this.status = status;
        this.delivering = delivering;

        this.deliveryTimeInverseComparator = new RideComparator(); //ride comparator
    }

    public Boolean getDelivering() {
        return delivering;
    }

    public void setDelivering(Boolean delivering) {
        this.delivering = delivering;
    }

    public String getOriginalRequestKey() {
        return originalRequestKey;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
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

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public double getKm(){ return this.km; }

    public void addKm(double km) { this.km += km; }


    //to create Set<Ride>
    @Override
    public int hashCode() {
        return this.orderID.hashCode();
    }
}
