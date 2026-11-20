package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.model.Customer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomerDao implements Dao<Customer> {

    private static Customer map(ResultSet rs) throws SQLException {
        return new Customer(rs.getLong("id"), rs.getString("name"),
                rs.getString("phone"), rs.getString("address"));
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        return Jdbc.query("SELECT id, name, phone, address FROM customer ORDER BY id", CustomerDao::map);
    }

    @Override
    public Customer findById(long id) throws SQLException {
        return Jdbc.queryOne("SELECT id, name, phone, address FROM customer WHERE id = ?",
                CustomerDao::map, id);
    }

    @Override
    public long insert(Customer c) throws SQLException {
        return Jdbc.insert("INSERT INTO customer(name, phone, address) VALUES (?,?,?)",
                c.name(), c.phone(), c.address());
    }

    @Override
    public boolean update(Customer c) throws SQLException {
        return Jdbc.update("UPDATE customer SET name = ?, phone = ?, address = ? WHERE id = ?",
                c.name(), c.phone(), c.address(), c.id()) > 0;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return Jdbc.update("DELETE FROM customer WHERE id = ?", id) > 0;
    }
}
