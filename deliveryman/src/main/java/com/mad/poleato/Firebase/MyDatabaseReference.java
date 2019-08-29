package com.mad.poleato.Firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is a wrapper for a Database reference to easily add and remove listeners.
 * It contains the reference with its related lists of value and child listeners
 */
public class MyDatabaseReference {


    private DatabaseReference reference;
    private List<ValueEventListener> valueListener;
    private List<ChildEventListener> childListener;


    /**
     * When new Firebase reference is created, the obkect of class MyDatabaseReference
     * is created, and the lists are initialized.
     */
    public MyDatabaseReference(DatabaseReference reference) {

        this.reference = reference;
        valueListener= new ArrayList<>();
        childListener= new ArrayList<>();
    }

    /**
     * Add to the list of child listeners the new one received by parameter,
     * and activate the child event listener
     */
    public void setChildListener(ChildEventListener childListener) {

        this.reference.addChildEventListener(childListener);
        this.childListener.add(childListener);
    }

    /**
     * Add to the list of value listeners the new one received by paramete,
     * and activate the value event listener
     */
    public void setValueListener(ValueEventListener valueListener) {

        this.reference.addValueEventListener(valueListener);
        this.valueListener.add(valueListener);
    }

    /**
     * Similar to the setValueListener method,
     * this one add to the list of value listeners the new one received by parameter,
     * and activate the listener for single value event
     */
    public void setSingleValueListener(ValueEventListener valueListener){

        this.reference.addListenerForSingleValueEvent(valueListener);
        this.valueListener.add(valueListener);
    }

    /**
     * This method returns the attribute reference of class MyDatabaseReference
     */
    public DatabaseReference getReference(){

        return reference;
    }


    /**
     * This method remove all listeners related to the reference of the calling object
     */
    public void removeAllListener(){

        for(ValueEventListener vl : valueListener){
            reference.removeEventListener(vl);
        }

        for(ChildEventListener cl : childListener){
            reference.removeEventListener(cl);
        }
    }
}