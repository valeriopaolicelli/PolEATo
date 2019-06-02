package com.mad.poleato.History;

import java.util.Comparator;
import java.util.Date;


/**
 * This comparator is used to compare two History objects based on Date in descending order
 * */
public class HistoryComparator implements Comparator<HistoryItem> {


    @Override
    public int compare(HistoryItem h1, HistoryItem h2) {

        Date d1 = new Date(h1.getDeliveredTime());
        Date d2 = new Date(h2.getDeliveredTime());

        //return inverse comparator
        return d1.compareTo(d2) * -1;
    }
}
