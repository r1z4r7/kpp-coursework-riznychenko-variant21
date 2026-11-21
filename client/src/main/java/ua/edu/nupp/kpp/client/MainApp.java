package ua.edu.nupp.kpp.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.ui.CourierTab;
import ua.edu.nupp.kpp.client.ui.CrudTab;
import ua.edu.nupp.kpp.client.ui.CustomerTab;
import ua.edu.nupp.kpp.client.ui.OrderTab;
import ua.edu.nupp.kpp.client.ui.ShipmentTab;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/** Головне вікно JavaFX-клієнта: чотири вкладки за основними сутностями варіанта 21. */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        setupLogging();
        ApiClient api = new ApiClient();

        OrderTab orders = new OrderTab(api);
        CustomerTab customers = new CustomerTab(api);
        CourierTab couriers = new CourierTab(api);
        ShipmentTab shipments = new ShipmentTab(api);

        TabPane tabs = new TabPane(orders, customers, couriers, shipments);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, old, tab) -> {
            if (tab instanceof CrudTab<?> crud) {
                crud.refresh();
            }
        });

        stage.setTitle("Кур'єрські доставки");
        stage.setScene(new Scene(tabs, 1180, 640));
        stage.show();

        orders.refresh();
        customers.refresh();
        couriers.refresh();
        shipments.refresh();
    }

    /** Журнал клієнта пишеться в client.log (відносний шлях, файл не потрапляє в Git). */
    private static void setupLogging() {
        try {
            FileHandler handler = new FileHandler("client.log", true);
            handler.setEncoding("UTF-8");
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("ua.edu.nupp.kpp").addHandler(handler);
        } catch (IOException e) {
            System.err.println("Не вдалося створити client.log: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
