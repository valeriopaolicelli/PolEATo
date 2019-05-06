package com.mad.poleato;


import com.google.firebase.database.Exclude;

import java.io.Serializable;


public class Food implements Serializable {

    private String img;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private Integer selectedQuantity;
    private String customerNotes;
    private String foodID;


    Food(String foodID, String img, String name, String description, Double price, int quantity){

        this.img = img;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.foodID = foodID;
        this.selectedQuantity = 0;
    }

    public String getFoodID() {
        return foodID;
    }

    public void setFoodID(String foodID) {
        this.foodID = foodID;
    }

    @Exclude
    public String getImg() {
        return img;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() { return quantity; }

    public void setImg(String img){ this.img = img; }

    public void setName(String name){ this.name = name; }

    public void setPrice(Double price){ this.price = price; }

    public void setDescription(String description){ this.description = description; }

    public void setQuantity(int quantity){ this.quantity = quantity; }

    public Integer getSelectedQuantity() {
        return selectedQuantity;
    }

    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = selectedQuantity;
    }
    public void increaseSelectedQuantity(){
        this.selectedQuantity += 1;
    }
    public void decreaseSelectedQuantity(){
        this.selectedQuantity -=1;
    }


    public String getCustomerNotes() {
        return customerNotes;
    }

    public void setCustomerNotes(String customerNotes) {
        this.customerNotes = customerNotes;
    }
}
