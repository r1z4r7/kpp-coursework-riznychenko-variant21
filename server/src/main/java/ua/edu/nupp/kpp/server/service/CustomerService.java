package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.CustomerDao;
import ua.edu.nupp.kpp.server.dao.Dao;
import ua.edu.nupp.kpp.server.model.Customer;

public class CustomerService extends CrudService<Customer> {

    private final CustomerDao dao;

    public CustomerService() {
        this(new CustomerDao());
    }

    public CustomerService(CustomerDao dao) {
        this.dao = dao;
    }

    @Override
    protected Dao<Customer> dao() {
        return dao;
    }

    @Override
    protected String entityName() {
        return "клієнта";
    }

    @Override
    protected long idOf(Customer c) {
        return c.id();
    }

    @Override
    public void validate(Customer c) {
        if (c == null) {
            throw new IllegalArgumentException("Порожні дані клієнта");
        }
        Check.notBlank(c.name(), "Ім'я", 160);
        Check.phone(c.phone());
        Check.notBlank(c.address(), "Адреса", 255);
    }
}
