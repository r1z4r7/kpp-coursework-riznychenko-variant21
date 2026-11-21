package ua.edu.nupp.kpp.client.importer;

import org.junit.jupiter.api.Test;
import ua.edu.nupp.kpp.client.model.Shipment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Тести розбору CSV відправлень: заголовок, кількість полів, типи й діапазони значень. */
class ShipmentCsvParserTest {

    @Test
    void parsesValidRow() {
        Shipment s = ShipmentCsvParser.parseRow("Книги;2.5;Книгарня «Склад»;20");
        assertEquals("Книги", s.description());
        assertEquals(2.5, s.weightKg(), 0.001);
        assertEquals("Книгарня «Склад»", s.senderName());
        assertEquals(20.0, s.fragility(), 0.001);
    }

    @Test
    void acceptsCommaAsDecimalSeparator() {
        Shipment s = ShipmentCsvParser.parseRow("Книги;2,5;Склад;20");
        assertEquals(2.5, s.weightKg(), 0.001);
    }

    @Test
    void rejectsWrongFieldCount() {
        assertThrows(IllegalArgumentException.class, () -> ShipmentCsvParser.parseRow("Ваза;1.5;Магазин"));
    }

    @Test
    void rejectsNonNumericWeight() {
        assertThrows(IllegalArgumentException.class, () -> ShipmentCsvParser.parseRow("Іграшка;abc;Магазин;30"));
    }

    @Test
    void rejectsFragilityOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> ShipmentCsvParser.parseRow("Скульптура;7;Галерея;150"));
    }

    @Test
    void rejectsEmptyDescription() {
        assertThrows(IllegalArgumentException.class, () -> ShipmentCsvParser.parseRow(" ;1;Склад;10"));
    }

    @Test
    void acceptsHeaderWithBom() {
        assertDoesNotThrow(() -> ShipmentCsvParser.requireHeader("\uFEFF" + ShipmentCsvParser.HEADER));
    }

    @Test
    void rejectsWrongHeader() {
        assertThrows(IllegalArgumentException.class,
                () -> ShipmentCsvParser.requireHeader("name;weight;sender;fragility"));
    }
}
