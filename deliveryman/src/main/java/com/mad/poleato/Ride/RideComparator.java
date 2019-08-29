package com.mad.poleato.Ride;

import java.util.Comparator;
import java.util.Date;


/**
 * This class is used to compare two Rider objects based on the delivery time in descending order
 */
public class RideComparator implements Comparator<Ride> {

    //sort the rides based on the delivery time
    @Override
    public int compare(Ride r1, Ride r2) {

        Date d1 = new Date(r1.getDeliveryTime());
        Date d2 = new Date(r2.getDeliveryTime());

        //return inverse comparator
        return d1.compareTo(d2) * -1;
    }
}
