package com.mad.poleato.Classes;

public class Rating {
    private String customerID;
    private Integer rate;
    private String comment;
    private String restaurantID;
    private String orderID;

    public Rating(String customerID, Integer rate, String comment, String restaurantID, String orderID) {
        this.customerID = customerID;
        this.rate = rate;
        this.comment = comment;
        this.restaurantID = restaurantID;
        this.orderID = orderID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }
}
