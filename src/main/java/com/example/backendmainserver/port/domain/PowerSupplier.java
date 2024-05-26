package com.example.backendmainserver.port.domain;

public enum PowerSupplier {
    EXTERNAL("EXTERNAL"), BATTERY("BATTERY"), OFF("OFF");

    private final String name;

    PowerSupplier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
