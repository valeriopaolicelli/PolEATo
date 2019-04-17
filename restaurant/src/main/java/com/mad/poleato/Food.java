package com.mad.poleato;

import android.graphics.Bitmap;
import android.os.Parcelable;
import android.widget.ImageView;

import org.w3c.dom.Text;

import java.io.Serializable;

public class Food implements Serializable {

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

    public int getQuantity() { return quantity; }

    public void setImg(Bitmap img){ this.img = img; }

    public void setName(String name){ this.name = name; }

    public void setPrice(Double price){ this.price = price; }

    public void setDescription(String description){ this.description = description; }

    public void setQuantity(int quantity){ this.quantity = quantity; }



    public boolean validation() {
        if(img != null && name.length() > 0 && description.length() > 0 && price > 0 && quantity > 0){
            return true;
        }else{
            return false;
        }
    }
}
