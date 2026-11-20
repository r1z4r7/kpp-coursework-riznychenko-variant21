package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.model.Courier;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CourierDao implements Dao<Courier> {

    private static Courier map(ResultSet rs) throws SQLException {
        return new Courier(rs.getLong("id"), rs.getString("name"),
                rs.getString("phone"), rs.getString("vehicle_type"));
    }

    @Override
    public List<Courier> findAll() throws SQLException {
        return Jdbc.query("SELECT id, name, phone, vehicle_type FROM courier ORDER BY id", CourierDao::map);
    }

    @Override
    public Courier findById(long id) throws SQLException {
        return Jdbc.queryOne("SELECT id, name, phone, vehicle_type FROM courier WHERE id = ?",
                CourierDao::map, id);
    }

    @Override
    public long insert(Courier c) throws SQLException {
        return Jdbc.insert("INSERT INTO courier(name, phone, vehicle_type) VALUES (?,?,?)",
                c.name(), c.phone(), c.vehicleType());
    }

    @Override
    public boolean update(Courier c) throws SQLException {
        return Jdbc.update("UPDATE courier SET name = ?, phone = ?, vehicle_type = ? WHERE id = ?",
                c.name(), c.phone(), c.vehicleType(), c.id()) > 0;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return Jdbc.update("DELETE FROM courier WHERE id = ?", id) > 0;
    }
}
