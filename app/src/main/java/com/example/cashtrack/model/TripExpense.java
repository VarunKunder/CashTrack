package com.example.cashtrack.model;

import java.util.List;

public class TripExpense {
    private long id;
    private long tripId;
    private String description;
    private double amount;
    private String date;
    private Participant paidBy;
    private List<Participant> sharedWith;

    public TripExpense(long id, long tripId, String description, double amount, String date, Participant paidBy, List<Participant> sharedWith) {
        this.id = id;
        this.tripId = tripId;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.paidBy = paidBy;
        this.sharedWith = sharedWith;
    }

    public long getId() {
        return id;
    }

    public long getTripId() {
        return tripId;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public Participant getPaidBy() {
        return paidBy;
    }

    public List<Participant> getSharedWith() {
        return sharedWith;
    }
}
