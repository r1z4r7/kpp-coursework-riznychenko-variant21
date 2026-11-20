package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.Dao;
import ua.edu.nupp.kpp.server.dao.ShipmentDao;
import ua.edu.nupp.kpp.server.model.Shipment;

public class ShipmentService extends CrudService<Shipment> {

    private final ShipmentDao dao;

    public ShipmentService() {
        this(new ShipmentDao());
    }

    public ShipmentService(ShipmentDao dao) {
        this.dao = dao;
    }

    @Override
    protected Dao<Shipment> dao() {
        return dao;
    }

    @Override
    protected String entityName() {
        return "відправлення";
    }

    @Override
    protected long idOf(Shipment s) {
        return s.id();
    }

    @Override
    public void validate(Shipment s) {
        if (s == null) {
            throw new IllegalArgumentException("Порожні дані відправлення");
        }
        Check.notBlank(s.description(), "Опис", 255);
        Check.positive(s.weightKg(), "Вага (кг)");
        Check.notBlank(s.senderName(), "Відправник", 160);
        Check.inRange(s.fragility(), 0, 100, "Чутливість вантажу");
    }
}
