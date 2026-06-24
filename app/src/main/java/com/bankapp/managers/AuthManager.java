package com.bankapp.managers;

import android.content.Context;

import com.bankapp.models.User;
import com.bankapp.utils.Constants;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class AuthManager {
    private static AuthManager instance;
    private final DataManager dataManager;
    private User currentUser;

    private AuthManager(Context context) {
        dataManager = DataManager.getInstance(context);
        // ИЗМЕНЕНИЕ: НЕ восстанавливаем сессию из SharedPreferences при создании.
        // Сессия сбрасывается при каждом запуске через SplashActivity.logout().
        currentUser = null;
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    // Хэширование пароля SHA-256
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback — не рекомендуется в продакшене
            return password;
        }
    }

    // Вход в систему
    public enum LoginResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        BANNED,
        USER_NOT_FOUND
    }

    public LoginResult login(String username, String password) {
        // Проверка суперадмина
        if (username.equals(Constants.SUPER_ADMIN_LOGIN)
                && password.equals(Constants.SUPER_ADMIN_PASS)) {

            User sa = dataManager.getUserById(Constants.SUPER_ADMIN_ID);
            if (sa == null) {
                sa = new User(
                        Constants.SUPER_ADMIN_ID,
                        Constants.SUPER_ADMIN_LOGIN,
                        hashPassword(Constants.SUPER_ADMIN_PASS),
                        "Главный Администратор",
                        "admin@bank.com",
                        "+70000000000",
                        true, true
                );
                dataManager.addUser(sa);
            }
            currentUser = sa;
            dataManager.saveCurrentUserId(sa.getId());
            return LoginResult.SUCCESS;
        }

        // Обычный пользователь
        User user = dataManager.getUserByUsername(username);
        if (user == null)      return LoginResult.USER_NOT_FOUND;
        if (user.isBanned())   return LoginResult.BANNED;
        if (!user.getPasswordHash().equals(hashPassword(password)))
            return LoginResult.INVALID_CREDENTIALS;

        currentUser = user;
        dataManager.saveCurrentUserId(user.getId());
        return LoginResult.SUCCESS;
    }

    // Регистрация
    public enum RegisterResult {
        SUCCESS,
        USERNAME_TAKEN,
        INVALID_DATA
    }

    public RegisterResult register(String username, String password,
                                   String fullName, String email, String phone) {
        if (username == null || username.isEmpty()
                || password == null || password.isEmpty()
                || fullName == null || fullName.isEmpty()) {
            return RegisterResult.INVALID_DATA;
        }

        if (dataManager.getUserByUsername(username) != null) {
            return RegisterResult.USERNAME_TAKEN;
        }

        String id = UUID.randomUUID().toString();
        User user = new User(
                id, username, hashPassword(password),
                fullName,
                email  != null ? email  : "",
                phone  != null ? phone  : "",
                false, false
        );
        dataManager.addUser(user);
        currentUser = user;
        dataManager.saveCurrentUserId(id);
        return RegisterResult.SUCCESS;
    }

    // Выход / текущий пользователь
    public void logout() {
        currentUser = null;
        dataManager.saveCurrentUserId(null);
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void refreshCurrentUser(Context context) {
        if (currentUser == null) return;
        String uid = dataManager.getCurrentUserId();
        if (uid != null) {
            User refreshed = dataManager.getUserById(uid);
            if (refreshed != null) currentUser = refreshed;
        }
    }
}
