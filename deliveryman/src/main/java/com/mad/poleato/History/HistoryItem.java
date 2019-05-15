package com.mad.poleato.History;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class HistoryItem {

    private String orderID;
    private String addressRestaurant;
    private String nameRestaurant;
    private String totalPrice;
    private String numberOfDishes;
    private String expectedTime;
    private String deliveredTime;
    private String deliveredDate;

    public HistoryItem(String orderID, String addressRestaurant, String nameRestaurant,
                       String totalPrice, String numberOfDishes,
                       String expectedTime, String deliveredTime, String deliveredDate) {
        this.orderID = orderID;
        this.addressRestaurant = addressRestaurant;
        this.nameRestaurant = nameRestaurant;
        this.totalPrice = totalPrice;
        this.numberOfDishes = numberOfDishes;
        this.expectedTime = expectedTime;
        this.deliveredTime = deliveredTime;
        this.deliveredDate= deliveredDate;
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

    public String getDeliveredDate() {
        return deliveredDate;
    }

    public static Comparator<HistoryItem> timeComparator= new Comparator<HistoryItem>() {
        @Override
        public int compare(HistoryItem r1, HistoryItem r2) {
            SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

            Date date1 = null, date2= null, time1= null, time2= null;

            try {
                date1 = formatDate.parse(r1.getDeliveredDate());
                date2 = formatDate.parse(r2.getDeliveredDate());
                time1= formatTime.parse(r1.getDeliveredTime());
                time2= formatTime.parse(r2.getDeliveredTime());
            } catch (ParseException e) {
                e.printStackTrace();
                return -1;
            }

            if(date1.compareTo(date2) < 0)
                return 1;
            else if(date1.compareTo(date2) > 0)
                return -1;
            else{
                /*
                 * Same date -> compare time
                 */

                if(time1.compareTo(time2) < 0)
                    return 1;
                else if(time1.compareTo(time2) > 0)
                    return -1;
                else // at same time and date
                    return 0;
            }
        }
    };
}
