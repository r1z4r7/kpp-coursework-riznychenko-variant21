package ua.edu.nupp.kpp.server.web;

import ua.edu.nupp.kpp.server.model.DeliveryOrder;
import ua.edu.nupp.kpp.server.model.TransitionRequest;
import ua.edu.nupp.kpp.server.service.CrudService;
import ua.edu.nupp.kpp.server.service.DeliveryOrderService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Замовлення доставки. Окрім CRUD:
 * POST /orders?action=transition&id=N з тілом {"state":"ASSIGNED","courierId":1} — зміна стану;
 * POST /orders?action=recalc&id=N — перерахунок інтегрального показника.
 */
@WebServlet("/orders")
public class DeliveryOrderServlet extends CrudServlet<DeliveryOrder> {

    private final DeliveryOrderService orders = new DeliveryOrderService();

    public DeliveryOrderServlet() {
        super(DeliveryOrder.class);
    }

    @Override
    protected CrudService<DeliveryOrder> crud() {
        return orders;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if ("transition".equals(action)) {
            handle(req, resp, HttpServletResponse.SC_OK, () -> {
                TransitionRequest t = readBody(req, TransitionRequest.class);
                return orders.transition(parseId(req.getParameter("id")), t.state(), t.courierId());
            });
        } else if ("recalc".equals(action)) {
            handle(req, resp, HttpServletResponse.SC_OK,
                    () -> orders.recalculate(parseId(req.getParameter("id"))));
        } else {
            super.doPost(req, resp);
        }
    }
}
