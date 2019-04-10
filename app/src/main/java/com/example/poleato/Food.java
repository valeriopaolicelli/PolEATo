package com.example.poleato;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.w3c.dom.Text;

public class Food {

    private Bitmap img;
    private String name;
    private String description;
    private Double price;
    private int quantity;

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

    public boolean validation() {
        if(img != null && name.length() > 0 && description.length() > 0 && price > 0 && quantity > 0){
            return true;
        }else{
            return false;
        }
    }
}
