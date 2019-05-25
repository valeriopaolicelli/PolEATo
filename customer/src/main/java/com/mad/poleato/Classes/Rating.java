package com.mad.poleato.Classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class Rating {
    private String customerID;
    private Integer rate;
    private String comment;
    private String restaurantID;
    private String orderID;
    private String customerData;
    private String date;

    public Rating(String customerID, Integer rate, String comment, String restaurantID, String orderID, String date) {
        this.customerID = customerID;
        this.rate = rate;
        this.comment = comment;
        this.restaurantID = restaurantID;
        this.orderID = orderID;
        this.date= date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public static Comparator<Rating> timeComparator= new Comparator<Rating>() {
        @Override
        public int compare(Rating r1, Rating r2) {
            SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

            Date date1 = null, date2= null;

            try {
                date1 = formatDate.parse(r1.getDate());
                date2 = formatDate.parse(r2.getDate());
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }

            return date1.compareTo(date2);
        }
    };

    public void setCustomerData(String customerData){
        this.customerData = customerData;
    }
    public String getCustomerdata(){
        return customerData;
    }
}
