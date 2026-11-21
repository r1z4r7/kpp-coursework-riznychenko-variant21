package ua.edu.nupp.kpp.client.ui;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Вкладка з таблицею та формою для однієї сутності. Дані завжди беруться із сервера:
 * після кожної операції таблиця перезавантажується, TableView не є сховищем.
 */
public abstract class CrudTab<T> extends Tab {

    private static final Logger LOG = Logger.getLogger(CrudTab.class.getName());

    protected final TableView<T> table = new TableView<>();
    protected final VBox form = new VBox(8);
    private final Label message = new Label();

    protected CrudTab(String title) {
        super(title);
        setClosable(false);
    }

    protected abstract List<T> load();

    protected abstract void onAdd();

    protected abstract void onUpdate(T selected);

    protected abstract void onDelete(T selected);

    protected abstract void fillForm(T selected);

    protected abstract void clearForm();

    protected final void addColumn(String title, double width, Function<T, String> value) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(d -> new ReadOnlyStringWrapper(value.apply(d.getValue())));
        table.getColumns().add(column);
    }

    /** Збирає вкладку: праворуч поля форми та кнопки CRUD, по центру таблиця. */
    protected final void build(Node... fields) {
        Button add = new Button("Додати");
        Button update = new Button("Змінити");
        Button delete = new Button("Видалити");
        Button clear = new Button("Очистити форму");
        Button refresh = new Button("Оновити");

        add.setOnAction(e -> act(() -> {
            onAdd();
            clearForm();
        }));
        update.setOnAction(e -> act(() -> onUpdate(selected())));
        delete.setOnAction(e -> act(() -> {
            onDelete(selected());
            clearForm();
        }));
        clear.setOnAction(e -> {
            table.getSelectionModel().clearSelection();
            clearForm();
        });
        refresh.setOnAction(e -> refresh());

        form.setPadding(new Insets(10));
        form.setPrefWidth(320);
        message.setWrapText(true);
        form.getChildren().addAll(fields);
        form.getChildren().addAll(new FlowPane(6, 6, add, update, delete, clear, refresh), message);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                fillForm(sel);
            }
        });

        BorderPane root = new BorderPane(table);
        root.setRight(form);
        setContent(root);
    }

    protected final T selected() {
        T value = table.getSelectionModel().getSelectedItem();
        if (value == null) {
            throw new IllegalStateException("Виберіть запис у таблиці");
        }
        return value;
    }

    /** Виконує операцію користувача: помилки показуються в діалозі та пишуться в журнал. */
    protected final void act(Runnable action) {
        try {
            action.run();
            refresh();
            setMessage("Готово", false);
        } catch (RuntimeException e) {
            String text = e.getMessage() == null ? e.toString() : e.getMessage();
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                LOG.warning("Некоректна дія користувача: " + text);
            } else {
                LOG.log(Level.WARNING, "Помилка операції: " + text);
            }
            setMessage(text, true);
            Ui.error(text);
        }
    }

    /** Перезавантажує таблицю із сервера; помилку показує тихо, у рядку статусу. */
    public void refresh() {
        try {
            table.setItems(FXCollections.observableArrayList(load()));
            setMessage("", false);
        } catch (RuntimeException e) {
            LOG.warning("Не вдалося оновити «" + getText() + "»: " + e.getMessage());
            setMessage(e.getMessage(), true);
        }
    }

    protected final void setMessage(String text, boolean error) {
        message.setText(text == null ? "" : text);
        message.setStyle(error ? "-fx-text-fill: #c0392b;" : "-fx-text-fill: #1e8449;");
    }
}
