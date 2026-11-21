package ua.edu.nupp.kpp.client.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.model.Courier;
import ua.edu.nupp.kpp.client.model.Customer;
import ua.edu.nupp.kpp.client.model.DeliveryOrder;
import ua.edu.nupp.kpp.client.model.OrderState;
import ua.edu.nupp.kpp.client.model.Shipment;

import java.util.List;

/**
 * Замовлення доставки: CRUD, зміна стану за життєвим циклом та перерахунок
 * інтегрального показника. Правила переходів і формулу застосовує сервер.
 */
public class OrderTab extends CrudTab<DeliveryOrder> {

    private final ApiClient api;
    private final ComboBox<Customer> customerBox = new ComboBox<>();
    private final ComboBox<Shipment> shipmentBox = new ComboBox<>();
    private final TextField planned = new TextField();
    private final TextField actual = new TextField();
    private final ComboBox<Courier> courierBox = new ComboBox<>();
    private final ComboBox<OrderState> stateBox = new ComboBox<>();

    public OrderTab(ApiClient api) {
        super("Замовлення доставки");
        this.api = api;

        addColumn("ID", 45, o -> String.valueOf(o.id()));
        addColumn("Клієнт", 60, o -> String.valueOf(o.customerId()));
        addColumn("Відправл.", 75, o -> String.valueOf(o.shipmentId()));
        addColumn("Кур'єр", 60, o -> o.courierId() == null ? "—" : String.valueOf(o.courierId()));
        addColumn("Стан", 95, o -> o.state().name());
        addColumn("План, год", 75, o -> Ui.fmt(o.plannedHours()));
        addColumn("Факт, год", 75, o -> Ui.fmt(o.actualHours()));
        addColumn("F1", 60, o -> Ui.fmt(o.f1()));
        addColumn("F2", 60, o -> Ui.fmt(o.f2()));
        addColumn("F3", 60, o -> Ui.fmt(o.f3()));
        addColumn("I", 65, o -> Ui.fmt(o.integralIndex()));

        customerBox.setConverter(Ui.converter(c -> c.id() + " — " + c.name()));
        shipmentBox.setConverter(Ui.converter(s -> s.id() + " — " + s.description()));
        courierBox.setConverter(Ui.converter(c -> c.id() + " — " + c.name()));
        stateBox.getItems().addAll(OrderState.values());
        stateBox.setValue(OrderState.ASSIGNED);

        build(Ui.labeled("Клієнт", customerBox), Ui.labeled("Відправлення", shipmentBox),
                Ui.labeled("Плановий час доставки, год", planned),
                Ui.labeled("Фактичний час, год (необов'язково)", actual));

        Button changeState = new Button("Змінити стан");
        Button recalc = new Button("Перерахувати I");
        changeState.setOnAction(e -> act(this::changeState));
        recalc.setOnAction(e -> act(() -> api.recalculate(selected().id())));
        form.getChildren().addAll(new Separator(),
                Ui.labeled("Кур'єр (для переходу в ASSIGNED)", courierBox),
                Ui.labeled("Новий стан", stateBox),
                new FlowPane(6, 6, changeState, recalc));
    }

    /** Окрім таблиці замовлень, оновлює списки клієнтів, відправлень і кур'єрів у формі. */
    @Override
    public void refresh() {
        try {
            customerBox.getItems().setAll(api.customers());
            shipmentBox.getItems().setAll(api.shipments());
            courierBox.getItems().setAll(api.couriers());
        } catch (RuntimeException ignored) {
            // повідомлення про недоступність сервера покаже super.refresh()
        }
        super.refresh();
    }

    @Override
    protected List<DeliveryOrder> load() {
        return api.orders();
    }

    @Override
    protected void onAdd() {
        api.addOrder(read(0));
    }

    @Override
    protected void onUpdate(DeliveryOrder selected) {
        api.updateOrder(read(selected.id()));
    }

    @Override
    protected void onDelete(DeliveryOrder selected) {
        api.deleteOrder(selected.id());
    }

    @Override
    protected void fillForm(DeliveryOrder o) {
        customerBox.setValue(customerBox.getItems().stream()
                .filter(c -> c.id() == o.customerId()).findFirst().orElse(null));
        shipmentBox.setValue(shipmentBox.getItems().stream()
                .filter(s -> s.id() == o.shipmentId()).findFirst().orElse(null));
        planned.setText(String.valueOf(o.plannedHours()));
        actual.setText(String.valueOf(o.actualHours()));
    }

    @Override
    protected void clearForm() {
        customerBox.setValue(null);
        shipmentBox.setValue(null);
        planned.clear();
        actual.clear();
    }

    private DeliveryOrder read(long id) {
        Customer customer = Ui.pick(customerBox, "Клієнт");
        Shipment shipment = Ui.pick(shipmentBox, "Відправлення");
        double plannedHours = Ui.number(planned, "Плановий час");
        double actualHours = Ui.numberOr(actual, "Фактичний час", 0);
        // стан, кур'єр і фактори на сервері розраховуються самостійно
        return new DeliveryOrder(id, customer.id(), shipment.id(), null, OrderState.CREATED,
                plannedHours, actualHours, 0, 0, 0, 0);
    }

    private void changeState() {
        DeliveryOrder order = selected();
        OrderState to = Ui.pick(stateBox, "Новий стан");
        Courier courier = courierBox.getValue();
        api.transition(order.id(), to, courier == null ? null : courier.id());
    }
}
