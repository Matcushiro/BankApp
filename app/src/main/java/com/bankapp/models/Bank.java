package com.bankapp.models;

import java.io.Serializable;
import java.util.Date;

public class Bank implements Serializable {
    private String name;
    private Date foundedDate;
    private String adminPassword;

    public Bank() {}

    public Bank(String name) {
        this.name = name;
        this.foundedDate = new Date();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getFoundedDate() { return foundedDate; }
    public void setFoundedDate(Date foundedDate) { this.foundedDate = foundedDate; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}