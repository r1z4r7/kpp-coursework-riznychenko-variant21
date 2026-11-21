package ua.edu.nupp.kpp.client.importer;

import javafx.concurrent.Task;
import ua.edu.nupp.kpp.client.api.ApiClient;
import ua.edu.nupp.kpp.client.api.ApiException;
import ua.edu.nupp.kpp.client.model.Shipment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Фоновий імпорт відправлень із CSV. Працює поза потоком JavaFX, рахує прогрес,
 * пропускає некоректні рядки та дублікати, а записи передає на сервер через ApiClient
 * (валідація та робота з MySQL залишаються на сервері).
 */
public class ImportShipmentsTask extends Task<ImportSummary> {

    private static final Logger LOG = Logger.getLogger(ImportShipmentsTask.class.getName());

    /** Невелика пауза між рядками, щоб прогрес було видно на демонстрації. Для швидкого імпорту поставте 0. */
    private static final long ROW_DELAY_MS = 150;

    private final Path file;
    private final ApiClient api;

    public ImportShipmentsTask(Path file, ApiClient api) {
        this.file = file;
        this.api = api;
    }

    @Override
    protected ImportSummary call() throws Exception {
        LOG.info("Початок імпорту CSV: " + file.getFileName());
        try {
            ImportSummary summary = process();
            LOG.info("Завершення імпорту CSV: рядків=" + summary.total() + ", імпортовано=" + summary.imported()
                    + ", дублікатів=" + summary.duplicates() + ", пропущено=" + summary.skipped());
            return summary;
        } catch (Exception e) {
            LOG.warning("Імпорт CSV перервано: " + e.getMessage());
            throw e;
        }
    }

    private ImportSummary process() throws Exception {
        updateMessage("Читання файлу…");
        List<String> lines = readLines();
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
        }
        if (lines.isEmpty()) {
            throw new IllegalStateException("Файл порожній");
        }
        ShipmentCsvParser.requireHeader(lines.get(0));

        updateMessage("Перевірка наявних відправлень…");
        Set<String> known = new HashSet<>();
        for (Shipment s : api.shipments()) {
            known.add(key(s));
        }

        int total = lines.size() - 1;
        int imported = 0;
        int duplicates = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 1; i <= total; i++) {
            int rowNumber = i + 1;
            try {
                String line = lines.get(i);
                if (line.isBlank()) {
                    throw new IllegalArgumentException("порожній рядок");
                }
                Shipment shipment = ShipmentCsvParser.parseRow(line);
                if (known.contains(key(shipment))) {
                    duplicates++;
                } else {
                    api.addShipment(shipment);
                    known.add(key(shipment));
                    imported++;
                }
            } catch (IllegalArgumentException e) {
                skipped++;
                errors.add("рядок " + rowNumber + ": " + e.getMessage());
                LOG.warning("Пропущено рядок " + rowNumber + ": " + e.getMessage());
            } catch (ApiException e) {
                if (e.isNetwork()) {
                    throw e;
                }
                skipped++;
                errors.add("рядок " + rowNumber + ": " + e.getMessage());
                LOG.warning("Сервер відхилив рядок " + rowNumber + ": " + e.getMessage());
            }
            updateProgress(i, total);
            updateMessage("Оброблено " + i + " з " + total);
            if (ROW_DELAY_MS > 0) {
                Thread.sleep(ROW_DELAY_MS);
            }
        }
        return new ImportSummary(total, imported, duplicates, skipped, errors);
    }

    private List<String> readLines() {
        try {
            return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (NoSuchFileException e) {
            throw new IllegalStateException("Файл не знайдено: " + file.getFileName());
        } catch (MalformedInputException e) {
            throw new IllegalStateException("Файл має бути у кодуванні UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати файл: " + e.getMessage());
        }
    }

    private static String key(Shipment s) {
        return (s.description().trim() + "|" + s.senderName().trim() + "|" + s.weightKg())
                .toLowerCase(Locale.ROOT);
    }
}
