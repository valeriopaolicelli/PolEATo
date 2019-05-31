package com.mad.poleato.Classes;


import java.io.Serializable;

/**
 * Class that regards restaurant
 */
public class Restaurant implements Serializable {

    private String id;
    private String image;
    private String name;
    private String type;
    private Boolean isOpen;
    private Integer priceRange;
    private Double deliveryCost;
    private Double avgStars;
    private Long totalReviews;

    public Restaurant(String id, String img, String name, String type, Boolean isOpen, int priceRange, double deliveryCost){
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

    public void setImage(String image) {
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

    public void setPriceRange(int priceRange) {
        this.priceRange = priceRange;
    }

    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
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

    public int getPriceRange() {
        return priceRange;
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }

    public void computeAvgStars(Integer totalStars){
        this.avgStars = ((double) totalStars/totalReviews);
    }

    public void setTotalReviews(Long totalReviews){
        this.totalReviews = totalReviews;
    }

    public Double getAvgStars(){
        return avgStars;
    }
    public Long getTotalReviews(){
        return totalReviews;
    }

}
