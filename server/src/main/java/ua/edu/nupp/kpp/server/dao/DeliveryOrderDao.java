package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.model.DeliveryOrder;
import ua.edu.nupp.kpp.server.model.OrderState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DeliveryOrderDao implements Dao<DeliveryOrder> {

    private static final String COLUMNS = "id, customer_id, shipment_id, courier_id, state, "
            + "planned_hours, actual_hours, f1, f2, f3, integral_index";

    private static DeliveryOrder map(ResultSet rs) throws SQLException {
        return new DeliveryOrder(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getLong("shipment_id"),
                rs.getObject("courier_id", Long.class),
                OrderState.valueOf(rs.getString("state")),
                rs.getDouble("planned_hours"),
                rs.getDouble("actual_hours"),
                rs.getDouble("f1"),
                rs.getDouble("f2"),
                rs.getDouble("f3"),
                rs.getDouble("integral_index"));
    }

    @Override
    public List<DeliveryOrder> findAll() throws SQLException {
        return Jdbc.query("SELECT " + COLUMNS + " FROM delivery_order ORDER BY id", DeliveryOrderDao::map);
    }

    @Override
    public DeliveryOrder findById(long id) throws SQLException {
        return Jdbc.queryOne("SELECT " + COLUMNS + " FROM delivery_order WHERE id = ?",
                DeliveryOrderDao::map, id);
    }

    @Override
    public long insert(DeliveryOrder o) throws SQLException {
        return Jdbc.insert("INSERT INTO delivery_order(customer_id, shipment_id, courier_id, state, "
                        + "planned_hours, actual_hours, f1, f2, f3, integral_index) VALUES (?,?,?,?,?,?,?,?,?,?)",
                o.customerId(), o.shipmentId(), o.courierId(), o.state().name(),
                o.plannedHours(), o.actualHours(), o.f1(), o.f2(), o.f3(), o.integralIndex());
    }

    @Override
    public boolean update(DeliveryOrder o) throws SQLException {
        return Jdbc.update("UPDATE delivery_order SET customer_id = ?, shipment_id = ?, courier_id = ?, state = ?, "
                        + "planned_hours = ?, actual_hours = ?, f1 = ?, f2 = ?, f3 = ?, integral_index = ? WHERE id = ?",
                o.customerId(), o.shipmentId(), o.courierId(), o.state().name(),
                o.plannedHours(), o.actualHours(), o.f1(), o.f2(), o.f3(), o.integralIndex(), o.id()) > 0;
    }

    @Override
    public boolean delete(long id) throws SQLException {
        return Jdbc.update("DELETE FROM delivery_order WHERE id = ?", id) > 0;
    }
}
