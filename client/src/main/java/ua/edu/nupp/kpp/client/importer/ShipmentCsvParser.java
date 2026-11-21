package ua.edu.nupp.kpp.client.importer;

import ua.edu.nupp.kpp.client.model.Shipment;

/**
 * Розбір CSV відправлень. Формат: роздільник «;», UTF-8, перший рядок — заголовок
 * description;weight_kg;sender_name;fragility. Усі помилки — IllegalArgumentException
 * з текстом, зрозумілим користувачу.
 */
public final class ShipmentCsvParser {

    public static final String HEADER = "description;weight_kg;sender_name;fragility";

    private ShipmentCsvParser() {
    }

    public static void requireHeader(String firstLine) {
        String header = firstLine == null ? "" : firstLine.replace("\uFEFF", "").trim();
        if (!header.equalsIgnoreCase(HEADER)) {
            throw new IllegalArgumentException("Неправильний заголовок CSV. Очікується: " + HEADER);
        }
    }

    public static Shipment parseRow(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("очікується 4 поля, знайдено " + parts.length);
        }
        String description = parts[0].trim();
        String sender = parts[2].trim();
        if (description.isEmpty()) {
            throw new IllegalArgumentException("порожній опис відправлення");
        }
        if (sender.isEmpty()) {
            throw new IllegalArgumentException("порожній відправник");
        }
        double weight = number(parts[1], "вага");
        double fragility = number(parts[3], "чутливість");
        if (!Double.isFinite(weight) || weight <= 0) {
            throw new IllegalArgumentException("вага має бути більше 0");
        }
        if (!(fragility >= 0 && fragility <= 100)) {
            throw new IllegalArgumentException("чутливість має бути в межах 0–100");
        }
        return new Shipment(0, description, weight, sender, fragility);
    }

    private static double number(String raw, String name) {
        try {
            return Double.parseDouble(raw.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " «" + raw.trim() + "» не є числом");
        }
    }
}
