package com.mad.poleato;

import android.graphics.Bitmap;

public class Restaurant {

    Bitmap image;
    String name;
    String type;
    Boolean isOpen;

    public Restaurant(Bitmap img, String name, String type, Boolean isOpen){
        setImage(img);
        setIsOpen(isOpen);
        setName(name);
        setType(type);

    }

    /** ** SETTER & GETTERS ** **/


    public void setImage(Bitmap image) {
        this.image = image;
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

    public Bitmap getImage() {
        return image;
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
}
