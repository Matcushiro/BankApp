package com.bankapp.models;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {

    public static final String TYPE_DEPOSIT    = "DEPOSIT";
    public static final String TYPE_WITHDRAW   = "WITHDRAW";
    public static final String TYPE_TRANSFER   = "TRANSFER";
    public static final String TYPE_INTEREST   = "INTEREST";

    private String id;
    private String type;
    private double amount;
    private String description;
    private Date date;
    private String fromAccountId;
    private String toAccountId;

    public Transaction() {
        this.date = new Date();
    }

    public Transaction(String id, String type, double amount,
                       String description, String fromAccountId, String toAccountId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.date = new Date();
    }

    public String getTypeDisplayName() {
        switch (type) {
            case TYPE_DEPOSIT:  return "Пополнение";
            case TYPE_WITHDRAW: return "Снятие";
            case TYPE_TRANSFER: return "Перевод";
            case TYPE_INTEREST: return "Начисление %";
            default:            return type;
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public String getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(String fromAccountId) { this.fromAccountId = fromAccountId; }

    public String getToAccountId() { return toAccountId; }
    public void setToAccountId(String toAccountId) { this.toAccountId = toAccountId; }
}