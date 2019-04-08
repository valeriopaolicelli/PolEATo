package com.example.poleato;

import android.content.Context;

import java.util.List;

enum Status{
    REJECTED, ACCEPATANCE, COOKING, DELIVERY
}
public class Reservation {
    private String order_id;
    private String name;
    private String surname;
    private String date;
    private Status status;
    private String stat;
    private List<Dish>dishes;

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;

    public Reservation(String order_id, String name, String surname, String date, String time, Context context) {
        this.order_id = order_id;
        this.name = name;
        this.surname = surname;
        this.date = date;
        this.time= time;
        this.status = Status.ACCEPATANCE;
        this.stat = context.getString(R.string.new_order);
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


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status, Context context) {
        if(status == Status.ACCEPATANCE)
            stat = context.getString(R.string.new_order);
        else if( status == Status.DELIVERY)
            stat = context.getString(R.string.delivery);
        else if( status == Status.COOKING)
            stat = context.getString(R.string.cooking);
        else if ( status == Status.REJECTED)
            stat = context.getString(R.string.reject);
        this.status = status;
    }

    public String getStat() {
        return stat;
    }

    public String getOrder_id() {
        return order_id;
    }
}
