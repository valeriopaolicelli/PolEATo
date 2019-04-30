package com.mad.poleato.Reservation;

import android.content.Context;
import android.util.Log;

import com.mad.poleato.R;

import java.util.ArrayList;



public class Reservation {
    private String order_id;
    private String name;
    private String surname;
    private String address;
    private String date;
    private Status status;
    private String stat;
    private String buttonText;
    private String phone;
    private boolean checked;

    private ArrayList<Dish>dishes = new ArrayList<>();

    public String getTime() {
        return time;
    }

    private String time;

    public Reservation(String order_id, String name, String surname, String address, String date, String time, String status, String phone, Context context) {
        this.order_id = order_id;
        this.name = name;
        this.surname = surname;
        this.address= address;
        this.date = date;
        this.time= time;
        this.stat = status;
        setStat(status, context);
        this.buttonText= context.getString(R.string.button_reservation);
        this.phone= phone;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setStatus(Status status, Context context) {
        if(status == Status.ACCEPATANCE)
            this.stat = context.getString(R.string.new_order);
        else if( status == Status.DELIVERY)
            this.stat = context.getString(R.string.delivery);
        else if( status == Status.COOKING)
            this.stat = context.getString(R.string.cooking);
        else if ( status == Status.REJECTED)
            this.stat = context.getString(R.string.reject);
        this.status = status;
    }

    public String getStat() {
        return stat;
    }

    public void setStat(String stat, Context context) {
        this.stat= stat;
        if(stat.equals(context.getString(R.string.new_order)))
            this.status= Status.ACCEPATANCE;
        if(stat.equals(context.getString(R.string.delivery)))
            this.status= Status.DELIVERY;
        if(stat.equals(context.getString(R.string.cooking)))
            this.status = Status.COOKING;
        if(stat.equals(context.getString(R.string.reject)))
            this.status= Status.REJECTED;
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getAddress(){ return address;}

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public ArrayList<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(ArrayList<Dish> dishes) {
        this.dishes = dishes;
    }
    public void addDishtoReservation(Dish d){
        this.dishes.add(d);
    }

    public String getButtonText(){
        return buttonText;
    }

    public void setButtonText(String text){
        this.buttonText= text;
    }

}
