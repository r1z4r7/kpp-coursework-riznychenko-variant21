package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

/**
 * Спільна CRUD-логіка Service-рівня: валідація перед викликом DAO та логування операцій.
 * Помилки даних — IllegalArgumentException, відсутній запис — NoSuchElementException.
 */
public abstract class CrudService<T> {

    protected final Logger log = Logger.getLogger(getClass().getName());

    protected abstract Dao<T> dao();

    /** Назва сутності для журналу, наприклад «клієнта». */
    protected abstract String entityName();

    protected abstract long idOf(T entity);

    /** Предметна валідація; не звертається до бази даних. */
    public abstract void validate(T entity);

    public List<T> list() throws SQLException {
        return dao().findAll();
    }

    public T get(long id) throws SQLException {
        T entity = dao().findById(id);
        if (entity == null) {
            throw new NoSuchElementException("Запис із id=" + id + " не знайдено");
        }
        return entity;
    }

    public T create(T entity) throws SQLException {
        validate(entity);
        long id = dao().insert(entity);
        log.info("Створено " + entityName() + ", id=" + id);
        return get(id);
    }

    public T update(T entity) throws SQLException {
        validate(entity);
        long id = idOf(entity);
        if (id <= 0) {
            throw new IllegalArgumentException("Не вказано id запису для редагування");
        }
        if (!dao().update(entity)) {
            throw new NoSuchElementException("Запис із id=" + id + " не знайдено");
        }
        log.info("Оновлено " + entityName() + ", id=" + id);
        return get(id);
    }

    public void delete(long id) throws SQLException {
        if (!dao().delete(id)) {
            throw new NoSuchElementException("Запис із id=" + id + " не знайдено");
        }
        log.info("Видалено " + entityName() + ", id=" + id);
    }
}
