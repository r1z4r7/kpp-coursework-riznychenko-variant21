package ua.edu.nupp.kpp.server.service;

/**
 * Інтегральний показник замовлення доставки: I = 0,40·F1 + 0,35·F2 + 0,25·F3.
 * <ul>
 *   <li>F1 — рівень сервісу / терміновість: чим коротше планове вікно доставки, тим вище F1;</li>
 *   <li>F2 — величина затримки відносно планового часу;</li>
 *   <li>F3 — чутливість вантажу (крихкість, 0–100).</li>
 * </ul>
 * Усі фактори нормуються до діапазону 0–100 і округлюються до сотих.
 */
public final class RiskCalculator {

    public static final double W1 = 0.40;
    public static final double W2 = 0.35;
    public static final double W3 = 0.25;

    /** Доставка за 24 години і довше вважається нетерміновою (F1 = 0). */
    public static final double URGENCY_WINDOW_HOURS = 24.0;

    private RiskCalculator() {
    }

    /** F1 = clamp((24 − planned) / 24 · 100, 0, 100). */
    public static double f1(double plannedHours) {
        if (!(plannedHours > 0)) {
            throw new IllegalArgumentException("Планований час має бути більше 0");
        }
        return round2(clamp((URGENCY_WINDOW_HOURS - plannedHours) / URGENCY_WINDOW_HOURS * 100.0));
    }

    /** F2 = clamp((actual − planned) / planned · 100, 0, 100). Дострокова доставка дає 0. */
    public static double f2(double plannedHours, double actualHours) {
        if (!(plannedHours > 0)) {
            throw new IllegalArgumentException("Планований час має бути більше 0");
        }
        if (!(actualHours >= 0)) {
            throw new IllegalArgumentException("Фактичний час не може бути від'ємним");
        }
        return round2(clamp((actualHours - plannedHours) / plannedHours * 100.0));
    }

    /** F3 = чутливість вантажу, вже задана в діапазоні 0–100. */
    public static double f3(double fragility) {
        requireFactor(fragility, "Чутливість вантажу");
        return round2(fragility);
    }

    public static double integral(double f1, double f2, double f3) {
        requireFactor(f1, "F1");
        requireFactor(f2, "F2");
        requireFactor(f3, "F3");
        return round2(W1 * f1 + W2 * f2 + W3 * f3);
    }

    static double clamp(double value) {
        return Math.max(0.0, Math.min(100.0, value));
    }

    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static void requireFactor(double value, String name) {
        if (!(value >= 0 && value <= 100)) {
            throw new IllegalArgumentException(name + " має бути в діапазоні 0–100");
        }
    }
}
