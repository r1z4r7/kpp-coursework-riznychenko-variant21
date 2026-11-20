package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.model.Shipment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ShipmentDao implements Dao<Shipment> {

    private static final String COLUMNS = "id, description, weight_kg, sender_name, fragility";

    private static Shipment map(ResultSet rs) throws SQLException {
        return new Shipment(rs.getLong("id"), rs.getString("description"),
                rs.getDouble("weight_kg"), rs.getString("sender_name"), rs.getDouble("fragility"));
    }

    @Override
    public List<Shipment> findAll() throws SQLException {
        return Jdbc.query("SELECT " + COLUMNS + " FROM shipment ORDER BY id", ShipmentDao::map);
    }

    @Override
    public Shipment findById(long id) throws SQLException {
        return Jdbc.queryOne("SELECT " + COLUMNS + " FROM shipment WHERE id = ?", ShipmentDao::map, id);
    }

    @Override
    public long insert(Shipment s) throws SQLException {
        return Jdbc.insert("INSERT INTO shipment(description, weight_kg, sender_name, fragility) VALUES (?,?,?,?)",
                s.description(), s.weightKg(), s.senderName(), s.fragility());
    }

    @Override
    public boolean update(Shipment s) throws SQLException {
        return Jdbc.update("UPDATE shipment SET description = ?, weight_kg = ?, sender_name = ?, fragility = ? WHERE id = ?",
                s.description(), s.weightKg(), s.senderName(), s.fragility(), s.id()) > 0;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return Jdbc.update("DELETE FROM shipment WHERE id = ?", id) > 0;
    }
}
