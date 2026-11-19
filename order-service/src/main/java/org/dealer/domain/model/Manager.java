package org.dealer.domain.model;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Manager {
    private final UUID managerID;
    private final String name;
    private final String email;

    public Manager(UUID managerID, String name, String email) {
        this.managerID = managerID;
        this.name = name;
        this.email = email;
    }
}