package com.mad.poleato.Classes;

import com.google.firebase.database.Exclude;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Class about the customer's reservation
 */

public class Reservation {
    private String orderID;
    private String restaurantName;
    private String restaurantID;
    private String date;
    private String time;
    private String totalPrice;
    private List<Dish> dishes;
    private boolean reviewFlag;
    private String Status;



    public Reservation(String orderID, String restaurantName, String date, String time,
                       String totalPrice) {
        this.orderID = orderID;
        this.restaurantName = restaurantName;
        this.date = date;
        this.time= time;
        this.totalPrice= totalPrice;
    }


    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
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

    public void setOrderID(String orderID){
        this.orderID=orderID;
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

    /**
     * Comparator to sort reviews by date
     */
    public static Comparator<Reservation> timeComparator= new Comparator<Reservation>() {
        @Override
        public int compare(Reservation r1, Reservation r2) {
            SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");

            Date date1 = null, date2= null, time1= null, time2= null;

            try {
                date1 = formatDate.parse(r1.getDate());
                date2 = formatDate.parse(r2.getDate());
                time1= formatTime.parse(r1.getTime());
                time2= formatTime.parse(r2.getTime());
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

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public boolean isReviewFlag() {
        return reviewFlag;
    }

    public void setReviewFlag(boolean reviewFlag) {
        this.reviewFlag = reviewFlag;
    }
}
