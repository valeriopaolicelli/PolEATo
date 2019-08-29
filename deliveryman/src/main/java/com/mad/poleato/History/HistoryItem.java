package com.mad.poleato.History;

import java.util.Comparator;


/**
 * This class represents a single history object
 */
public class HistoryItem {

    private String orderID;
    private String addressRestaurant;
    private String nameRestaurant;
    private String totalPrice;
    private String numberOfDishes;
    private String expectedTime;
    private String deliveredTime;
    private HistoryItemOutcome outcome;
    public static Comparator<HistoryItem> timeInverseComparator;


    public HistoryItem(String orderID, String addressRestaurant, String nameRestaurant,
                       String totalPrice, String numberOfDishes,
                       String expectedTime, String deliveredTime, HistoryItemOutcome outcome) {
        this.orderID = orderID;
        this.addressRestaurant = addressRestaurant;
        this.nameRestaurant = nameRestaurant;
        this.totalPrice = totalPrice;
        this.numberOfDishes = numberOfDishes;
        this.expectedTime = expectedTime;
        this.deliveredTime = deliveredTime;
        this.outcome = outcome;

        timeInverseComparator = new HistoryComparator(); //reverse order
    }

    public HistoryItemOutcome getOutcome() {
        return outcome;
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

    public String getDeliveredTime() {
        return deliveredTime;
    }

    public String getDeliveredHour(){ return  deliveredTime.split(" ")[1];}

    public String getDeliveredDate(){ return deliveredTime.split(" ")[0];}

    public String getExpectedHour(){ return expectedTime.split(" ")[1]; }


}
