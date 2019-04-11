package com.example.poleato;

public class Dish {

    // REFERENCE TO FOOD CLASS
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

    public Dish(String name, int quantity, String notes) {
        this.name = name;
        this.quantity = quantity;
        this.notes = notes;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
