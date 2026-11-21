package ua.edu.nupp.kpp.client.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.importer.ImportShipmentsTask;
import ua.edu.nupp.kpp.client.importer.ImportSummary;
import ua.edu.nupp.kpp.client.importer.ShipmentCsvParser;
import ua.edu.nupp.kpp.client.model.Shipment;

import java.io.File;
import java.util.List;

/** Відправлення: CRUD та фоновий імпорт із CSV (JavaFX Task + ProgressBar). */
public class ShipmentTab extends CrudTab<Shipment> {

    private final ApiClient api;
    private final TextField description = new TextField();
    private final TextField weight = new TextField();
    private final TextField sender = new TextField();
    private final TextField fragility = new TextField();

    private final Button importButton = new Button("Імпортувати CSV…");
    private final ProgressBar progress = new ProgressBar(0);
    private final Label importStatus = new Label("Імпорт не запускався");
    private boolean importing;

    public ShipmentTab(ApiClient api) {
        super("Відправлення");
        this.api = api;
        fragility.setPromptText("0–100");
        addColumn("ID", 50, s -> String.valueOf(s.id()));
        addColumn("Опис", 200, Shipment::description);
        addColumn("Вага, кг", 80, s -> Ui.fmt(s.weightKg()));
        addColumn("Відправник", 190, Shipment::senderName);
        addColumn("Чутливість (F3)", 110, s -> Ui.fmt(s.fragility()));
        build(Ui.labeled("Опис", description), Ui.labeled("Вага, кг", weight),
                Ui.labeled("Відправник", sender), Ui.labeled("Чутливість вантажу (0–100)", fragility));

        Label format = new Label("Формат CSV (UTF-8, роздільник «;»):\n" + ShipmentCsvParser.HEADER);
        format.setWrapText(true);
        progress.setMaxWidth(Double.MAX_VALUE);
        importButton.setOnAction(e -> startImport());
        form.getChildren().addAll(new Separator(), new Label("Імпорт відправлень із CSV"), format,
                importButton, progress, importStatus);
    }

    @Override
    protected List<Shipment> load() {
        return api.shipments();
    }

    @Override
    protected void onAdd() {
        api.addShipment(read(0));
    }

    @Override
    protected void onUpdate(Shipment selected) {
        api.updateShipment(read(selected.id()));
    }

    @Override
    protected void onDelete(Shipment selected) {
        api.deleteShipment(selected.id());
    }

    @Override
    protected void fillForm(Shipment s) {
        description.setText(s.description());
        weight.setText(String.valueOf(s.weightKg()));
        sender.setText(s.senderName());
        fragility.setText(String.valueOf(s.fragility()));
    }

    @Override
    protected void clearForm() {
        description.clear();
        weight.clear();
        sender.clear();
        fragility.clear();
    }

    private Shipment read(long id) {
        return new Shipment(id, Ui.text(description, "Опис"), Ui.number(weight, "Вага"),
                Ui.text(sender, "Відправник"), Ui.number(fragility, "Чутливість вантажу"));
    }

    // ---------- фоновий CSV-імпорт ----------
    private void startImport() {
        if (importing) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Виберіть CSV-файл відправлень");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV-файли", "*.csv"));
        File samples = new File("sample-data").getAbsoluteFile();
        if (samples.isDirectory()) {
            chooser.setInitialDirectory(samples);
        }
        File file = chooser.showOpenDialog(getTabPane().getScene().getWindow());
        if (file == null) {
            return;
        }

        ImportShipmentsTask task = new ImportShipmentsTask(file.toPath(), api);
        importing = true;
        importButton.setDisable(true);
        progress.progressProperty().bind(task.progressProperty());
        importStatus.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(e -> finishImport(task.getValue(), null));
        task.setOnFailed(e -> finishImport(null, task.getException()));

        Thread thread = new Thread(task, "csv-import");
        thread.setDaemon(true);
        thread.start();
    }

    private void finishImport(ImportSummary summary, Throwable error) {
        progress.progressProperty().unbind();
        importStatus.textProperty().unbind();
        importButton.setDisable(false);
        importing = false;
        if (error != null) {
            progress.setProgress(0);
            String text = error.getMessage() == null ? error.toString() : error.getMessage();
            importStatus.setText("Помилка імпорту");
            Ui.error(text);
        } else {
            progress.setProgress(1);
            importStatus.setText(summary.shortText());
            Ui.info("Результат імпорту", summary.report());
        }
        refresh();
    }
}
