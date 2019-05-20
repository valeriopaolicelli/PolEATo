package com.mad.poleato.Reservation;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;


public class Reservation implements Serializable{
    private String order_id;
    private String customer_id;
    private String name;
    private String surname;
    private String address;
    private String date;
    private Status status;
    private String stat;
    private String buttonText;
    private String phone;
    private boolean checked;
    private String totalPrice;
    private String locale;


    private ArrayList<Dish>dishes = new ArrayList<>();

    public String getTime() {
        return time;
    }

    private String time;

    public Reservation(String order_id, String customer_id, String name, String surname,
                       String address, String date, String time,
                       String status, String phone, String totalPrice, String locale){
        this.order_id = order_id;
        this.customer_id = customer_id;
        this.name = name;
        this.surname = surname;
        this.address= address;
        this.date = date;
        this.time= time;
        this.stat = status;
        if(stat!=null && (!stat.equals("")))
            setStat(status);
        this.totalPrice= totalPrice;
        this.locale= locale;
        if(locale.equals("it"))
            this.buttonText= "Accetta o Rifiuta";
        else
            this.buttonText= "Accept or Reject";
        this.phone= phone;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomerID(){
        return this.customer_id;
    }

    public void setCustomerID(String customer_id) {
        this.customer_id = customer_id;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhone(){return phone;}

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if(status == Status.ACCEPTANCE){
            if(locale.equals("it"))
                this.stat= "Nuovo ordine";
            else
                this.stat= "New order";
        }
        else if( status == Status.DELIVERY){
            if(locale.equals("it"))
                this.stat= "In consegna";
            else
                this.stat= "Delivering";
        }
        else if( status == Status.COOKING){
            if(locale.equals("it"))
                this.stat= "Preparazione";
            else
                this.stat= "Cooking";
        }
        else if ( status == Status.REJECTED){
            if(locale.equals("it"))
                this.stat= "Rifiutato";
            else
                this.stat= "Rejected";
        }
        else if ( status == Status.DELIVERED){
            if(locale.equals("it"))
                this.stat= "Consegnato";
            else
                this.stat= "Delivered";
        }
        this.status = status;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat) {
        this.stat= stat;
        if(stat.equals("New order") || stat.equals("Nuovo ordine"))
            this.status= Status.ACCEPTANCE;
        if(stat.equals("Delivering") || stat.equals("In consegna"))
            this.status= Status.DELIVERY;
        if(stat.equals("Cooking") || stat.equals("Preparazione"))
            this.status = Status.COOKING;
        if(stat.equals("Rejected") || stat.equals("Rifiutato"))
            this.status= Status.REJECTED;
        if(stat.equals("Delivered") || stat.equals("Consegnato"))
            this.status= Status.DELIVERED;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getAddress(){ return address;}

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public int getNumberOfDishes(){ return dishes.size(); }

    public void addDishtoReservation(Dish d){
        this.dishes.add(d);
    }

    public void setButtonText(String text){
        this.buttonText= text;
    }

    public void setDishes(ArrayList<Dish>dishes){
        this.dishes=dishes;
    }

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
                return -1;
            else if(date1.compareTo(date2) > 0)
                return 1;
            else{
                /*
                 * Same date -> compare time
                 */

                if(time1.compareTo(time2) < 0)
                    return -1;
                else if(time1.compareTo(time2) > 0)
                    return 1;
                else // at same time and date
                    return 0;
            }
        }
    };

    public static Comparator<Reservation> timeComparatorReverse= new Comparator<Reservation>() {
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
}
