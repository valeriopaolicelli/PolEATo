package com.mad.poleato;

import com.mad.poleato.OrderManagement.Order;

/**
 * Interface used to retrieve data in different parts of the application
 */
public interface Interface {
    public Order getOrder();
    public void setQuantity(int quantity);
}

