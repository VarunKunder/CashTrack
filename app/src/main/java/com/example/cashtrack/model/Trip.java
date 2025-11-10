package com.example.cashtrack.model;

public class Trip {
    private long id;
    private String name;

    public Trip(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
