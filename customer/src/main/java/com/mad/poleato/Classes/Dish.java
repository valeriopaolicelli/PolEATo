package com.mad.poleato.Classes;

public class Dish {

    private String name;
    private Integer quantity;
    private String notes;

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

    public Dish(String name, int quantity, String notes) {
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
    }
}
