package com.company.ckechout;

import com.company.Customer;

public interface CheckoutService {

    boolean checkout(Customer customer, Double totalAmount);
}
