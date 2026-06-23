package com.bankapp.utils;

public class ValidationUtils {
    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= 3 && username.matches("[a-zA-Z0-9_]+");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\+?[0-9]{10,15}");
    }

    public static boolean isValidAmount(String amountStr) {
        try {
            double amount = Double.parseDouble(amountStr);
            return amount > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}