package ua.edu.nupp.kpp.client.ui;

import javafx.scene.control.TextField;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.model.Customer;

import java.util.List;

public class CustomerTab extends CrudTab<Customer> {

    private final ApiClient api;
    private final TextField name = new TextField();
    private final TextField phone = new TextField();
    private final TextField address = new TextField();

    public CustomerTab(ApiClient api) {
        super("Клієнти");
        this.api = api;
        phone.setPromptText("+380501112233");
        addColumn("ID", 50, c -> String.valueOf(c.id()));
        addColumn("Ім'я", 180, Customer::name);
        addColumn("Телефон", 130, Customer::phone);
        addColumn("Адреса", 260, Customer::address);
        build(Ui.labeled("Ім'я", name), Ui.labeled("Телефон", phone), Ui.labeled("Адреса", address));
    }

    @Override
    protected List<Customer> load() {
        return api.customers();
    }

    @Override
    protected void onAdd() {
        api.addCustomer(read(0));
    }

    @Override
    protected void onUpdate(Customer selected) {
        api.updateCustomer(read(selected.id()));
    }

    @Override
    protected void onDelete(Customer selected) {
        api.deleteCustomer(selected.id());
    }

    @Override
    protected void fillForm(Customer c) {
        name.setText(c.name());
        phone.setText(c.phone());
        address.setText(c.address());
    }

    @Override
    protected void clearForm() {
        name.clear();
        phone.clear();
        address.clear();
    }

    private Customer read(long id) {
        return new Customer(id, Ui.text(name, "Ім'я"), Ui.text(phone, "Телефон"), Ui.text(address, "Адреса"));
    }
}
