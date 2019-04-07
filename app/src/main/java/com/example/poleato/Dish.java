package com.example.poleato;

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

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Dish(String name, int quantity, String notes) {
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
    }
}
