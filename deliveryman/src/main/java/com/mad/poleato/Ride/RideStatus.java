package com.mad.poleato.Ride;

/**
 * The status of the ride;
 *  - TO_RESTAURANT: the rider is directed to the restaurant
 *  - TO_CUSTOMER: the rider is directed to the customer
 *  - BACKWARD: failure with the deliver. The rider is coming back to restaurant
 */
public enum RideStatus{

    TO_RESTAURANT,
    TO_CUSTOMER,
    BACKWARD
}
