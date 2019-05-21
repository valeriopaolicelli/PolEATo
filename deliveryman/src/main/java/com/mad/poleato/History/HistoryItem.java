package com.mad.poleato.History;

import java.util.Comparator;

public class HistoryItem {

    private String orderID;
    private String addressRestaurant;
    private String nameRestaurant;
    private String totalPrice;
    private String numberOfDishes;
    private String expectedTime;
    private String deliveredTime;
    public static Comparator<HistoryItem> timeInverseComparator;

    public HistoryItem(String orderID, String addressRestaurant, String nameRestaurant,
                       String totalPrice, String numberOfDishes,
                       String expectedTime, String deliveredTime) {
        this.orderID = orderID;
        this.addressRestaurant = addressRestaurant;
        this.nameRestaurant = nameRestaurant;
        this.totalPrice = totalPrice;
        this.numberOfDishes = numberOfDishes;
        this.expectedTime = expectedTime;
        this.deliveredTime = deliveredTime;

        timeInverseComparator = new HistoryComparator(); //reverse order
    }

    public String getOrderID() {
        return orderID;
    }

    public String getAddressRestaurant() {
        return addressRestaurant;
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

    public String getExpectedTime() {
        return expectedTime;
    }

    public String getDeliveredTime() {
        return deliveredTime;
    }

    public String getDeliveredHour(){ return  deliveredTime.split(" ")[1];}

    public String getDeliveredDate(){ return deliveredTime.split(" ")[0];}

    public String getExpectedHour(){ return expectedTime.split(" ")[1]; }

    public String getExpectedDate() { return  expectedTime.split(" ")[0]; }


}
