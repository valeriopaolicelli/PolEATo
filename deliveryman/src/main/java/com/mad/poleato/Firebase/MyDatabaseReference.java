package com.mad.poleato.Firebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * This class is a wrapper for a Database reference to easily add and remove listeners
 */
public class MyDatabaseReference {

    /**
     * This class is a wrapper for a Database reference to easily add and remove listeners
     */

    private DatabaseReference reference;
    private List<ValueEventListener> valueListener;
    private List<ChildEventListener> childListener;

    public MyDatabaseReference(DatabaseReference reference) {
        this.reference = reference;
        valueListener= new ArrayList<>();
        childListener= new ArrayList<>();
    }

    public void setChildListener(ChildEventListener childListener) {
        this.reference.addChildEventListener(childListener);
        this.childListener.add(childListener);
    }

    public void setValueListener(ValueEventListener valueListener) {
        this.reference.addValueEventListener(valueListener);
        this.valueListener.add(valueListener);
    }

    public void setSingleValueListener(ValueEventListener valueListener){
        this.reference.addListenerForSingleValueEvent(valueListener);
        this.valueListener.add(valueListener);
    }

    public DatabaseReference getReference(){
        return reference;
    }

    public void removeAllListener(){
        for(ValueEventListener vl : valueListener){
            reference.removeEventListener(vl);
        }

        for(ChildEventListener cl : childListener){
            reference.removeEventListener(cl);
        }
    }
}