package ua.edu.nupp.kpp.server.service;

import java.util.Locale;
import java.util.regex.Pattern;

/** Допоміжні перевірки для валідації на рівні Service. */
final class Check {

    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ()\\-]{7,20}$");

    private Check() {
    }

    static void notBlank(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле «" + field + "» не може бути порожнім");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException("Поле «" + field + "» задовге (максимум "
                    + maxLength + " символів)");
        }
    }

    static void phone(String value) {
        notBlank(value, "Телефон", 30);
        if (!PHONE.matcher(value.trim()).matches()) {
            throw new IllegalArgumentException("Телефон має бути у форматі +380501112233");
        }
    }

    static void positive(double value, String field) {
        if (!(value > 0)) {
            throw new IllegalArgumentException("Значення «" + field + "» має бути більше 0");
        }
    }

    static void nonNegative(double value, String field) {
        if (!(value >= 0)) {
            throw new IllegalArgumentException("Значення «" + field + "» не може бути від'ємним");
        }
    }

    static void inRange(double value, double min, double max, String field) {
        if (!(value >= min && value <= max)) {
            throw new IllegalArgumentException(String.format(Locale.ROOT,
                    "Значення «%s» має бути в межах %.0f–%.0f", field, min, max));
        }
    }
}
