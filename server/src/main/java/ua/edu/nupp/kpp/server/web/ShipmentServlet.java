package ua.edu.nupp.kpp.server.web;

import ua.edu.nupp.kpp.server.model.Shipment;
import ua.edu.nupp.kpp.server.service.CrudService;
import ua.edu.nupp.kpp.server.service.ShipmentService;

import javax.servlet.annotation.WebServlet;

@WebServlet("/shipments")
public class ShipmentServlet extends CrudServlet<Shipment> {

    private final ShipmentService service = new ShipmentService();

    public ShipmentServlet() {
        super(Shipment.class);
    }

    @Override
    protected CrudService<Shipment> crud() {
        return service;
    }
}
