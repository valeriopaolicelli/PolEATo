package com.mad.poleato;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseReference {

    /**
     * This class is a wrapper for a Database reference to easily add and remove listeners.
     * It contains the reference with its related lists of value and child listeners
     */

    private DatabaseReference reference;
    private List<ValueEventListener> valueListener;
    private List<ChildEventListener> childListener;

    public MyDatabaseReference(DatabaseReference reference) {
        /**
         * When new Firebase reference is created, the obkect of class MyDatabaseReference
         * is created, and the lists are initialized.
         */
        this.reference = reference;
        valueListener= new ArrayList<>();
        childListener= new ArrayList<>();
    }

    public void setChildListener(ChildEventListener childListener) {
        /**
         * Add to the list of child listeners the new one received by parameter,
         * and activate the child event listener
         */
        this.reference.addChildEventListener(childListener);
        this.childListener.add(childListener);
    }

    public void setValueListener(ValueEventListener valueListener) {
        /**
         * Add to the list of value listeners the new one received by paramete,
         * and activate the value event listener
         */
        this.reference.addValueEventListener(valueListener);
        this.valueListener.add(valueListener);
    }

    public void setSingleValueListener(ValueEventListener valueListener){
        /**
         * Similar to the setValueListener method,
         * this one add to the list of value listeners the new one received by parameter,
         * and activate the listener for single value event
         */
        this.reference.addListenerForSingleValueEvent(valueListener);
        this.valueListener.add(valueListener);
    }

    public DatabaseReference getReference(){
        /**
         * This method returns the attribute reference of class MyDatabaseReference
         */
        return reference;
    }

    public void removeAllListener(){
        /**
         * This method remove all listeners related to the reference of the calling object
         */
        for(ValueEventListener vl : valueListener){
            reference.removeEventListener(vl);
        }

        for(ChildEventListener cl : childListener){
            reference.removeEventListener(cl);
        }
    }
}