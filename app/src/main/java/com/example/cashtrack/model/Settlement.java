package com.example.cashtrack.model;

public class Settlement {
    private final String from;
    private final String to;
    private final double amount;

    public Settlement(String from, String to, double amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public double getAmount() {
        return amount;
    }
}
