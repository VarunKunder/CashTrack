package com.example.cashtrack.model;

public class Expense {
    private long id;
    private String category;
    private double amount;
    private String date;
    private String note;

    public Expense(long id, String category, double amount, String date, String note) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    public long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }
}
