package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.CourierDao;
import ua.edu.nupp.kpp.server.dao.CustomerDao;
import ua.edu.nupp.kpp.server.dao.Dao;
import ua.edu.nupp.kpp.server.dao.DeliveryOrderDao;
import ua.edu.nupp.kpp.server.dao.ShipmentDao;
import ua.edu.nupp.kpp.server.model.DeliveryOrder;
import ua.edu.nupp.kpp.server.model.OrderState;
import ua.edu.nupp.kpp.server.model.Shipment;

import java.sql.SQLException;

/**
 * Замовлення доставки: валідація, перевірка пов'язаних записів, життєвий цикл
 * та розрахунок інтегрального показника. Фактори F1–F3 і I обчислює лише сервер.
 */
public class DeliveryOrderService extends CrudService<DeliveryOrder> {

    private final DeliveryOrderDao orderDao;
    private final CustomerDao customerDao;
    private final ShipmentDao shipmentDao;
    private final CourierDao courierDao;

    public DeliveryOrderService() {
        this(new DeliveryOrderDao(), new CustomerDao(), new ShipmentDao(), new CourierDao());
    }

    public DeliveryOrderService(DeliveryOrderDao orderDao, CustomerDao customerDao,
                                ShipmentDao shipmentDao, CourierDao courierDao) {
        this.orderDao = orderDao;
        this.customerDao = customerDao;
        this.shipmentDao = shipmentDao;
        this.courierDao = courierDao;
    }

    @Override
    protected Dao<DeliveryOrder> dao() {
        return orderDao;
    }

    @Override
    protected String entityName() {
        return "замовлення доставки";
    }

    @Override
    protected long idOf(DeliveryOrder o) {
        return o.id();
    }

    @Override
    public void validate(DeliveryOrder o) {
        if (o == null) {
            throw new IllegalArgumentException("Порожні дані замовлення");
        }
        if (o.customerId() <= 0) {
            throw new IllegalArgumentException("Не вказано клієнта");
        }
        if (o.shipmentId() <= 0) {
            throw new IllegalArgumentException("Не вказано відправлення");
        }
        Check.positive(o.plannedHours(), "Планований час (год)");
        Check.nonNegative(o.actualHours(), "Фактичний час (год)");
    }

    /** Нове замовлення завжди починається зі стану CREATED без кур'єра. */
    @Override
    public DeliveryOrder create(DeliveryOrder in) throws SQLException {
        validate(in);
        requireCustomer(in.customerId());
        Shipment shipment = requireShipment(in.shipmentId());
        DeliveryOrder order = withFactors(new DeliveryOrder(0, in.customerId(), in.shipmentId(), null,
                OrderState.CREATED, in.plannedHours(), in.actualHours(), 0, 0, 0, 0), shipment.fragility());
        long id = orderDao.insert(order);
        log.info("Створено замовлення доставки, id=" + id + ", I=" + order.integralIndex());
        return get(id);
    }

    /** Редагування не змінює стан і кур'єра: для цього є {@link #transition}. */
    @Override
    public DeliveryOrder update(DeliveryOrder in) throws SQLException {
        validate(in);
        if (in.id() <= 0) {
            throw new IllegalArgumentException("Не вказано id запису для редагування");
        }
        DeliveryOrder old = get(in.id());
        requireCustomer(in.customerId());
        Shipment shipment = requireShipment(in.shipmentId());
        DeliveryOrder updated = withFactors(new DeliveryOrder(old.id(), in.customerId(), in.shipmentId(),
                old.courierId(), old.state(), in.plannedHours(), in.actualHours(), 0, 0, 0, 0),
                shipment.fragility());
        orderDao.update(updated);
        log.info("Оновлено замовлення доставки, id=" + old.id() + ", I=" + updated.integralIndex());
        return get(old.id());
    }

    /** Зміна стану за правилами життєвого циклу; для ASSIGNED обов'язково вказується кур'єр. */
    public DeliveryOrder transition(long id, OrderState to, Long courierId) throws SQLException {
        if (to == null) {
            throw new IllegalArgumentException("Не вказано новий стан");
        }
        DeliveryOrder order = get(id);
        OrderLifecycle.requireTransition(order.state(), to);

        Long courier = order.courierId();
        if (to == OrderState.ASSIGNED) {
            if (courierId == null || courierId <= 0) {
                throw new IllegalArgumentException("Для призначення потрібно вказати кур'єра");
            }
            if (courierDao.findById(courierId) == null) {
                throw new IllegalArgumentException("Кур'єра з id=" + courierId + " не існує");
            }
            courier = courierId;
        }
        Shipment shipment = requireShipment(order.shipmentId());
        DeliveryOrder next = withFactors(new DeliveryOrder(order.id(), order.customerId(), order.shipmentId(),
                courier, to, order.plannedHours(), order.actualHours(), 0, 0, 0, 0), shipment.fragility());
        orderDao.update(next);
        log.info("Змінено стан замовлення id=" + id + ": " + order.state() + " → " + to);
        return get(id);
    }

    /** Перерахунок F1–F3 та I (наприклад, після зміни чутливості вантажу). */
    public DeliveryOrder recalculate(long id) throws SQLException {
        DeliveryOrder order = get(id);
        Shipment shipment = requireShipment(order.shipmentId());
        orderDao.update(withFactors(order, shipment.fragility()));
        log.info("Перераховано інтегральний показник замовлення id=" + id);
        return get(id);
    }

    /** Повертає копію замовлення з обчисленими F1, F2, F3 та інтегральним показником. */
    public static DeliveryOrder withFactors(DeliveryOrder o, double fragility) {
        double f1 = RiskCalculator.f1(o.plannedHours());
        double f2 = RiskCalculator.f2(o.plannedHours(), o.actualHours());
        double f3 = RiskCalculator.f3(fragility);
        double index = RiskCalculator.integral(f1, f2, f3);
        return new DeliveryOrder(o.id(), o.customerId(), o.shipmentId(), o.courierId(), o.state(),
                o.plannedHours(), o.actualHours(), f1, f2, f3, index);
    }

    private void requireCustomer(long id) throws SQLException {
        if (customerDao.findById(id) == null) {
            throw new IllegalArgumentException("Клієнта з id=" + id + " не існує");
        }
    }

    private Shipment requireShipment(long id) throws SQLException {
        Shipment shipment = shipmentDao.findById(id);
        if (shipment == null) {
            throw new IllegalArgumentException("Відправлення з id=" + id + " не існує");
        }
        return shipment;
    }
}
