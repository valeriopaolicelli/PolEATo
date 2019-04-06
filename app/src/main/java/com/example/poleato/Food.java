package com.example.poleato;

import android.graphics.Bitmap;
import android.widget.ImageView;

import org.w3c.dom.Text;

public class Food {

    public Food(Bitmap img, String name, String description, Double price){

        this.img = img;
        this.name = name;
        this.description = description;
        this.price = price;
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

    private Bitmap img;
    private String name;
    private String description;
    private Double price;
}
