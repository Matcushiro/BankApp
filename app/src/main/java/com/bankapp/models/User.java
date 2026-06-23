package com.bankapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User implements Serializable {
    private String id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private String phone;
    private boolean isAdmin;
    private boolean isSuperAdmin;
    private boolean isBanned;
    private String createdByAdminId; // null = super admin or self-registered
    private Date registrationDate;
    private List<Account> accounts;

    public User() {
        this.accounts = new ArrayList<>();
        this.registrationDate = new Date();
    }

    public User(String id, String username, String passwordHash,
                String fullName, String email, String phone,
                boolean isAdmin, boolean isSuperAdmin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.isSuperAdmin = isSuperAdmin;
        this.isBanned = false;
        this.registrationDate = new Date();
        this.accounts = new ArrayList<>();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public boolean isSuperAdmin() { return isSuperAdmin; }
    public void setSuperAdmin(boolean superAdmin) { isSuperAdmin = superAdmin; }

    public boolean isBanned() { return isBanned; }
    public void setBanned(boolean banned) { isBanned = banned; }

    public String getCreatedByAdminId() { return createdByAdminId; }
    public void setCreatedByAdminId(String createdByAdminId) { this.createdByAdminId = createdByAdminId; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date date) { this.registrationDate = date; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    public double getTotalBalance() {
        double total = 0;
        for (Account acc : accounts) total += acc.getBalance();
        return total;
    }

    public Account getAccountByType(String type) {
        for (Account acc : accounts) {
            if (acc.getType().equals(type)) return acc;
        }
        return null;
    }

    public boolean hasAccountOfType(String type) {
        return getAccountByType(type) != null;
    }
}