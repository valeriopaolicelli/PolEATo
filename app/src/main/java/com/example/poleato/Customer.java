package com.example.poleato;

public class Customer {
    private String name;
    private String surname;
    private String notes;
    private String date;

    public Customer(String name, String surname, String notes, String date) {
        this.name = name;
        this.surname = surname;
        this.notes = notes;
        this.date = date;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
