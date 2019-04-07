package com.example.poleato;

import java.util.List;

enum Status{
    REJECTED, ACCEPATANCE, COOKING, DELIVERY;
}
public class Customer {
    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    private String order_id;
    private String name;
    private String surname;
    private String notes;
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

    public Customer(String order_id, String name, String surname, String notes, String date) {
        this.order_id = order_id;
        this.name = name;
        this.surname = surname;
        this.notes = notes;
        this.date = date;
        this.status = Status.ACCEPATANCE;
        this.stat = "On acceptance";
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public void setStatus(Status status) {
        if(status == Status.ACCEPATANCE)
            stat = "On acceptance";
        else if( status == Status.DELIVERY)
            stat = "On delivery";
        else if( status == Status.COOKING)
            stat = "Cooking";
        else if ( status == Status.REJECTED)
            stat = "Rejected";

        this.status = status;
    }

    public String getStat() {
        return stat;
    }
}
