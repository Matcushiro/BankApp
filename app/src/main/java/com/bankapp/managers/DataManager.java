package com.bankapp.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.bankapp.models.Account;
import com.bankapp.models.Bank;
import com.bankapp.models.User;
import com.bankapp.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static DataManager instance;
    private final SharedPreferences prefs;
    private final Gson gson;

    // Кэш в памяти
    private Bank bank;
    private List<User> users;

    private DataManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        gson  = new Gson();
        loadData();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context);
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────
    // Загрузка данных из SharedPreferences
    // ─────────────────────────────────────────────────────────
    private void loadData() {
        String bankJson  = prefs.getString(Constants.KEY_BANK_DATA, null);
        String usersJson = prefs.getString(Constants.KEY_USERS_DATA, null);

        bank = bankJson != null ? gson.fromJson(bankJson, Bank.class) : null;

        Type listType = new TypeToken<List<User>>(){}.getType();
        users = usersJson != null
                ? gson.fromJson(usersJson, listType)
                : new ArrayList<>();

        if (users == null) users = new ArrayList<>();
    }

    // ─────────────────────────────────────────────────────────
    // Сохранение всех данных
    // ─────────────────────────────────────────────────────────
    public void saveAll() {
        prefs.edit()
                .putString(Constants.KEY_BANK_DATA,  gson.toJson(bank))
                .putString(Constants.KEY_USERS_DATA, gson.toJson(users))
                .apply();
    }

    // ─────────────────────────────────────────────────────────
    // Банк
    // ─────────────────────────────────────────────────────────
    public Bank getBank() { return bank; }

    public void setBank(Bank bank) {
        this.bank = bank;
        saveAll();
    }

    public boolean isBankInitialized() { return bank != null; }

    // ─────────────────────────────────────────────────────────
    // Пользователи
    // ─────────────────────────────────────────────────────────
    public List<User> getAllUsers() { return users; }

    public User getUserById(String id) {
        if (id == null) return null;
        for (User u : users) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public User getUserByUsername(String username) {
        if (username == null) return null;
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public void addUser(User user) {
        users.add(user);
        saveAll();
    }

    /**
     * Обновляет пользователя в списке и сразу сохраняет в SharedPreferences.
     * Ключевой метод — все изменения баланса/счетов проходят через него.
     */
    public void updateUser(User updated) {
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updated.getId())) {
                users.set(i, updated);
                found = true;
                break;
            }
        }
        if (!found) {
            // Если пользователя не было — добавляем
            users.add(updated);
        }
        // Немедленно сохраняем на диск
        saveAll();
    }

    public void deleteUser(String userId) {
        users.removeIf(u -> u.getId().equals(userId));
        saveAll();
    }

    // ─────────────────────────────────────────────────────────
    // Счета
    // ─────────────────────────────────────────────────────────
    public Account findAccountById(String accountId) {
        if (accountId == null) return null;
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                if (a.getId().equals(accountId)) return a;
            }
        }
        return null;
    }

    public User findUserByAccountId(String accountId) {
        if (accountId == null) return null;
        for (User u : users) {
            for (Account a : u.getAccounts()) {
                if (a.getId().equals(accountId)) return u;
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────
    // Статистика банка
    // ─────────────────────────────────────────────────────────
    public double getTotalBankBalance() {
        double total = 0;
        for (User u : users)
            for (Account a : u.getAccounts())
                total += a.getBalance();
        return total;
    }

    public int getTotalAccountsCount() {
        int count = 0;
        for (User u : users) count += u.getAccounts().size();
        return count;
    }

    // ─────────────────────────────────────────────────────────
    // Сессия текущего пользователя
    // ─────────────────────────────────────────────────────────
    public void saveCurrentUserId(String userId) {
        prefs.edit().putString(Constants.KEY_CURRENT_USER, userId).apply();
    }

    public String getCurrentUserId() {
        return prefs.getString(Constants.KEY_CURRENT_USER, null);
    }

    public void clearSession() {
        prefs.edit().remove(Constants.KEY_CURRENT_USER).apply();
    }
}
