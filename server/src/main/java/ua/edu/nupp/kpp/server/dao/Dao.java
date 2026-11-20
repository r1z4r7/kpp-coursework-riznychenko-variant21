package ua.edu.nupp.kpp.server.dao;

import java.sql.SQLException;
import java.util.List;

public interface Dao<T> {
    List<T> findAll() throws SQLException;

    T findById(long id) throws SQLException;

    long insert(T entity) throws SQLException;

    boolean update(T entity) throws SQLException;

    boolean delete(long id) throws SQLException;
}
