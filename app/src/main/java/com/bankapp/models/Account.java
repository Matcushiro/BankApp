package com.bankapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Account implements Serializable {

    public static final String TYPE_DEBIT      = "DEBIT";
    public static final String TYPE_CREDIT     = "CREDIT";
    public static final String TYPE_SAVINGS    = "SAVINGS";

    public static final double CREDIT_LIMIT    = 50000.0;
    public static final double SAVINGS_RATE    = 0.08; // 8% per month
    public static final long   SAVINGS_PERIOD_MS = 30L * 24 * 60 * 60 * 1000; // 30 days

    private String id;
    private String userId;
    private String type;
    private double balance;
    private double creditLimit;
    private double interestRate;
    private Date createdDate;
    private Date nextInterestDate;
    private boolean isActive;
    private List<Transaction> transactions;

    public Account() {
        this.transactions = new ArrayList<>();
        this.isActive = true;
        this.createdDate = new Date();
    }

    public Account(String id, String userId, String type) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.isActive = true;
        this.createdDate = new Date();
        this.transactions = new ArrayList<>();

        switch (type) {
            case TYPE_DEBIT:
                this.balance = 0;
                this.creditLimit = 0;
                this.interestRate = 0;
                break;
            case TYPE_CREDIT:
                this.balance = 0;
                this.creditLimit = CREDIT_LIMIT;
                this.interestRate = 0.15; // 15% per year
                break;
            case TYPE_SAVINGS:
                this.balance = 0;
                this.creditLimit = 0;
                this.interestRate = SAVINGS_RATE;
                this.nextInterestDate = new Date(System.currentTimeMillis() + SAVINGS_PERIOD_MS);
                break;
        }
    }

    public String getTypeDisplayName() {
        switch (type) {
            case TYPE_DEBIT:   return "Дебетовый";
            case TYPE_CREDIT:  return "Кредитный";
            case TYPE_SAVINGS: return "Накопительный";
            default:           return type;
        }
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getNextInterestDate() { return nextInterestDate; }
    public void setNextInterestDate(Date nextInterestDate) { this.nextInterestDate = nextInterestDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public double getAvailableCredit() {
        if (type.equals(TYPE_CREDIT)) return creditLimit + balance;
        return 0;
    }
}