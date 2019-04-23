package com.mad.poleato;

import android.graphics.Bitmap;

public class Restaurant {

    String id;
    Bitmap image;
    String name;
    String type;
    Boolean isOpen;

    public Restaurant(String id, Bitmap img, String name, String type, Boolean isOpen){
        setId(id);
        setImage(img);
        setIsOpen(isOpen);
        setName(name);
        setType(type);

    }

    /** ** SETTER & GETTERS ** **/


    public void setId(String id){
        this.id = id;
    }

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

    public String getId() {
        return id;
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
