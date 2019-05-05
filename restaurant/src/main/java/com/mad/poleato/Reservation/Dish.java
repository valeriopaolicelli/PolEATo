package com.mad.poleato.Reservation;

public class Dish {

    // REFERENCE TO FOOD CLASS
    private String ID;
    private String name;
    private Integer quantity;
    private String notes;
    private boolean checked;

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

    public Dish(String foodID, String name, int quantity, String notes) {
        this.ID = foodID;
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
        checked= false;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
