package com.example.cashtrack.model;

public class Participant {
    private long id;
    private long tripId;
    private String name;

    public Participant(long id, long tripId, String name) {
        this.id = id;
        this.tripId = tripId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public long getTripId() {
        return tripId;
    }

    public String getName() {
        return name;
    }
}
