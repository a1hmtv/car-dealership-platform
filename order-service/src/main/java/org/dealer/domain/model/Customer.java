package org.dealer.domain.model;

import lombok.Getter;
import org.dealer.domain.exceptions.DomainValidationException;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

@Getter

public class Customer {
    private final UUID customerID;
    private final String name;
    private final String email;
    private final String phone;
    private final String address;

    private final List<InStockOrder> inStockOrders = new ArrayList<>();
    private final List<CustomOrder> customOrders = new ArrayList<>();

    public Customer(UUID customerID, String name, String email, String phone, String address){
        if (customerID == null) throw new DomainValidationException("customerID must be not null");
        if (name == null || name.isBlank()) throw new DomainValidationException("name must be not blank");
        if (email == null || email.isBlank()) throw new DomainValidationException("email must be not blank");
        if (phone == null || phone.isBlank()) throw new DomainValidationException("phone must be not blank");
        if (address == null || address.isBlank()) throw new DomainValidationException("address must be not blank");

        this.customerID = customerID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public List<InStockOrder> getInStockOrders() {
        return Collections.unmodifiableList(inStockOrders);
    }

    public List<CustomOrder> getCustomOrders() {
        return Collections.unmodifiableList(customOrders);
    }

    public void addInStockOrder(InStockOrder order) {
        if (order == null) throw new DomainValidationException("order must be not null");
        inStockOrders.add(order);
    }

    public void addCustomOrder(CustomOrder order) {
        if (order == null) throw new DomainValidationException("order must be not null");
        customOrders.add(order);
    }
}

