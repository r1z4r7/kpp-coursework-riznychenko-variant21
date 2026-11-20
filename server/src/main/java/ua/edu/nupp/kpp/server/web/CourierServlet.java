package ua.edu.nupp.kpp.server.web;

import ua.edu.nupp.kpp.server.model.Courier;
import ua.edu.nupp.kpp.server.service.CourierService;
import ua.edu.nupp.kpp.server.service.CrudService;

import javax.servlet.annotation.WebServlet;

@WebServlet("/couriers")
public class CourierServlet extends CrudServlet<Courier> {

    private final CourierService service = new CourierService();

    public CourierServlet() {
        super(Courier.class);
    }

    @Override
    protected CrudService<Courier> crud() {
        return service;
    }
}
