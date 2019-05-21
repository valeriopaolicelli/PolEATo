package com.mad.poleato.History;

import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

public class HistoryComparator implements Comparator<HistoryItem> {


    @Override
    public int compare(HistoryItem h1, HistoryItem h2) {

        Date d1 = new Date(h1.getDeliveredTime());
        Date d2 = new Date(h2.getDeliveredTime());

        //return inverse comparator
        return d1.compareTo(d2) * -1;
    }
}
