package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Підключення до MySQL. Параметри беруться зі змінних середовища
 * DB_URL, DB_USER, DB_PASSWORD; значення за замовчуванням підходять для локального XAMPP.
 */
public final class DBUtil {

    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/courier_delivery?useUnicode=true&characterEncoding=UTF-8");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBUtil() {
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
