package com.example.project1;

public class Expense {
    private int id;
    private double amount;
    private String description;
    private String category;
    private String date;
    private String time;

    public Expense(int id, double amount, String description, String category, String date, String time) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.date = date;
        this.time = time;
    }

    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getDate() { return date; }
    public String getTime() { return time; }
}