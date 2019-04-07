package com.example.poleato;

import java.util.List;

enum Status{
    REJECTED, ACCEPATANCE, COOKING, DELIVERY;
}
public class Customer {
    private String name;
    private String surname;
    private String notes;
    private String date;
    private Status status;
    private String stat;
    private List<Dish>dishes;

    public Customer(String name, String surname, String notes, String date) {
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
