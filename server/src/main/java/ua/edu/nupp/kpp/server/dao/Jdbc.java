package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Спільні JDBC-операції. Усі значення передаються через PreparedStatement
 * (SQL не формується конкатенацією), ресурси закриваються через try-with-resources.
 */
final class Jdbc {

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    static <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, params);
            try (ResultSet rs = p.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        }
    }

    static <T> T queryOne(String sql, RowMapper<T> mapper, Object... params) throws SQLException {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    static long insert(String sql, Object... params) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(p, params);
            p.executeUpdate();
            try (ResultSet keys = p.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0;
            }
        }
    }

    static int update(String sql, Object... params) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, params);
            return p.executeUpdate();
        }
    }

    private static void bind(PreparedStatement p, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            p.setObject(i + 1, params[i]);
        }
    }
}
