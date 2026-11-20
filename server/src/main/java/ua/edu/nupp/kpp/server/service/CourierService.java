package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.CourierDao;
import ua.edu.nupp.kpp.server.dao.Dao;
import ua.edu.nupp.kpp.server.model.Courier;

public class CourierService extends CrudService<Courier> {

    private final CourierDao dao;

    public CourierService() {
        this(new CourierDao());
    }

    public CourierService(CourierDao dao) {
        this.dao = dao;
    }

    @Override
    protected Dao<Courier> dao() {
        return dao;
    }

    @Override
    protected String entityName() {
        return "кур'єра";
    }

    @Override
    protected long idOf(Courier c) {
        return c.id();
    }

    @Override
    public void validate(Courier c) {
        if (c == null) {
            throw new IllegalArgumentException("Порожні дані кур'єра");
        }
        Check.notBlank(c.name(), "Ім'я", 160);
        Check.phone(c.phone());
        Check.notBlank(c.vehicleType(), "Тип транспорту", 60);
    }
}
