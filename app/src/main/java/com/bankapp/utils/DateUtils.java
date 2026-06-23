package com.bankapp.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public static String formatDate(Date date) {
        if (date == null) return "—";
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "—";
        return DATE_TIME_FORMAT.format(date);
    }

    public static long daysUntil(Date date) {
        if (date == null) return 0;
        long diff = date.getTime() - System.currentTimeMillis();
        return Math.max(0, diff / (1000 * 60 * 60 * 24));
    }
}