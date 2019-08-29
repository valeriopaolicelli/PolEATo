package com.mad.poleato.Reservation;

import java.io.Serializable;


/**
 * Class that describes every dish present in a single Reservation
 */
public class Dish implements Serializable {

    private String name;
    private Integer quantity;
    private String notes;
    private boolean checked;
    private String id;

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }

    public Dish(String name, int quantity, String notes, String foodID) {
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
        checked= false;
        this.id= foodID;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
