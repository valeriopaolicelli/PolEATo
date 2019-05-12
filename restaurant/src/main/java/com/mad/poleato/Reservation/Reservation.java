package com.mad.poleato.Reservation;

import android.content.Context;
import android.util.Log;

import com.mad.poleato.R;

import java.io.Serializable;
import java.util.ArrayList;



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
    }

    public String getOrder_id() {
        return order_id;
    }

    public String getAddress(){ return address;}

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
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

    public int getNumberOfDishes(){ return dishes.size(); }

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
