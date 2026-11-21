package ua.edu.nupp.kpp.client.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.Locale;
import java.util.function.Function;

/** Допоміжні методи інтерфейсу: підписані поля, перевірка введення, діалоги. */
final class Ui {

    private Ui() {
    }

    static VBox labeled(String title, Control control) {
        if (control instanceof ComboBox<?> box) {
            box.setMaxWidth(Double.MAX_VALUE);
        }
        return new VBox(2, new Label(title), control);
    }

    /** Обов'язкове текстове поле (клієнтська валідація). */
    static String text(TextField field, String name) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Заповніть поле «" + name + "»");
        }
        return value;
    }

    /** Обов'язкове числове поле; кома приймається як десятковий роздільник. */
    static double number(TextField field, String name) {
        String value = text(field, name).replace(',', '.');
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле «" + name + "» має бути числом");
        }
    }

    static double numberOr(TextField field, String name, double defaultValue) {
        String value = field.getText() == null ? "" : field.getText().trim();
        return value.isEmpty() ? defaultValue : number(field, name);
    }

    static <T> T pick(ComboBox<T> box, String name) {
        T value = box.getValue();
        if (value == null) {
            throw new IllegalArgumentException("Виберіть значення «" + name + "»");
        }
        return value;
    }

    static String fmt(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    static <T> StringConverter<T> converter(Function<T, String> toText) {
        return new StringConverter<>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : toText.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
    }

    static void error(String message) {
        show(Alert.AlertType.ERROR, "Помилка", message);
    }

    static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(440);
        alert.showAndWait();
    }
}
