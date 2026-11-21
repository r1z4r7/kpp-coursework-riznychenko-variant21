package ua.edu.nupp.kpp.client.ui;

import javafx.scene.control.TextField;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.model.Courier;

import java.util.List;

public class CourierTab extends CrudTab<Courier> {

    private final ApiClient api;
    private final TextField name = new TextField();
    private final TextField phone = new TextField();
    private final TextField vehicle = new TextField();

    public CourierTab(ApiClient api) {
        super("Кур'єри");
        this.api = api;
        phone.setPromptText("+380931234567");
        addColumn("ID", 50, c -> String.valueOf(c.id()));
        addColumn("Ім'я", 200, Courier::name);
        addColumn("Телефон", 130, Courier::phone);
        addColumn("Транспорт", 150, Courier::vehicleType);
        build(Ui.labeled("Ім'я", name), Ui.labeled("Телефон", phone), Ui.labeled("Тип транспорту", vehicle));
    }

    @Override
    protected List<Courier> load() {
        return api.couriers();
    }

    @Override
    protected void onAdd() {
        api.addCourier(read(0));
    }

    @Override
    protected void onUpdate(Courier selected) {
        api.updateCourier(read(selected.id()));
    }

    @Override
    protected void onDelete(Courier selected) {
        api.deleteCourier(selected.id());
    }

    @Override
    protected void fillForm(Courier c) {
        name.setText(c.name());
        phone.setText(c.phone());
        vehicle.setText(c.vehicleType());
    }

    @Override
    protected void clearForm() {
        name.clear();
        phone.clear();
        vehicle.clear();
    }

    private Courier read(long id) {
        return new Courier(id, Ui.text(name, "Ім'я"), Ui.text(phone, "Телефон"),
                Ui.text(vehicle, "Тип транспорту"));
    }
}
