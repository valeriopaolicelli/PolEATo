package com.example.poleato;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.w3c.dom.Text;

public class Food {

    public Food(Bitmap img, String name, String description, Double price, int quantity){

        this.img = img;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public Bitmap getImg() {
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

    public int getQuantity() {
        return quantity;
    }

    private Bitmap img;
    private String name;
    private String description;
    private Double price;
    private int quantity;
}
