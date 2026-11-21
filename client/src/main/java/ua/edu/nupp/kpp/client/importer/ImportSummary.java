package ua.edu.nupp.kpp.client.importer;

import java.util.List;

/** Підсумок CSV-імпорту. */
public record ImportSummary(int total, int imported, int duplicates, int skipped, List<String> errors) {

    public String shortText() {
        return "Готово: імпортовано " + imported + ", дублікатів " + duplicates + ", пропущено " + skipped;
    }

    public String report() {
        StringBuilder sb = new StringBuilder();
        sb.append("Рядків у файлі: ").append(total).append('\n')
                .append("Імпортовано: ").append(imported).append('\n')
                .append("Дублікати (вже є в базі): ").append(duplicates).append('\n')
                .append("Пропущено через помилки: ").append(skipped);
        if (!errors.isEmpty()) {
            sb.append("\n\nПомилки:");
            errors.stream().limit(10).forEach(e -> sb.append("\n• ").append(e));
            if (errors.size() > 10) {
                sb.append("\n… та ще ").append(errors.size() - 10);
            }
        }
        return sb.toString();
    }
}
