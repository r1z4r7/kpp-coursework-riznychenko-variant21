package ua.edu.nupp.kpp.server.web;

import ua.edu.nupp.kpp.server.model.Customer;
import ua.edu.nupp.kpp.server.service.CrudService;
import ua.edu.nupp.kpp.server.service.CustomerService;

import javax.servlet.annotation.WebServlet;

@WebServlet("/customers")
public class CustomerServlet extends CrudServlet<Customer> {

    private final CustomerService service = new CustomerService();

    public CustomerServlet() {
        super(Customer.class);
    }

    @Override
    protected CrudService<Customer> crud() {
        return service;
    }
}
