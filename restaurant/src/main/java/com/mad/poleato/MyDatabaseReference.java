package com.mad.poleato;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseReference {

    private DatabaseReference reference;
    private List<ValueEventListener> valueListener;
    private List<ChildEventListener> childListener;

    public MyDatabaseReference(DatabaseReference reference) {
        this.reference = reference;
        valueListener= new ArrayList<>();
        childListener= new ArrayList<>();
    }

    public void setChildListener(ChildEventListener childListener) {
        this.childListener.add(childListener);
    }

    public void setValueListener(ValueEventListener valueListener) {
        this.valueListener.add(valueListener);
    }

    public DatabaseReference getReference(){
        return reference;
    }

    public void removeAllListener(){
        for(int i=0; i < valueListener.size(); i++){
            reference.removeEventListener(valueListener.get(i));
        }

        for(int i=0; i < childListener.size(); i++){
            reference.removeEventListener(childListener.get(i));
        }
    }
}