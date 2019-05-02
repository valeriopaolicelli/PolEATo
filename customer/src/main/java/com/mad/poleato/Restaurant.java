package com.mad.poleato;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Restaurant implements Serializable {

    String id;
    SerialBitmap image;
    String name;
    String type;
    Boolean isOpen;
    Integer priceRange;
    Double deliveryCost;

    public Restaurant(String id, Bitmap img, String name, String type, Boolean isOpen, int priceRange, double deliveryCost){
        setId(id);
        setImage(img);
        setIsOpen(isOpen);
        setName(name);
        setType(type);
        setPriceRange(priceRange);
        setDeliveryCost(deliveryCost);

    }

    /** ** SETTER & GETTERS ** **/


    public void setId(String id){
        this.id = id;
    }

    public void setImage(Bitmap image) {
        this.image = new SerialBitmap(image);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIsOpen(Boolean open) {
        this.isOpen = open;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPriceRange(int priceRange) {
        this.priceRange = priceRange;
    }

    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public String getId() {
        return id;
    }

    public Bitmap getImage() {
        return image.getBitmap();
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getPriceRange() {
        return priceRange;
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }
}
