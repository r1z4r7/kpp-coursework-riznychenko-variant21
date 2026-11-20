package ua.edu.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;
import ua.edu.nupp.kpp.server.model.Customer;
import ua.edu.nupp.kpp.server.model.DeliveryOrder;
import ua.edu.nupp.kpp.server.model.OrderState;
import ua.edu.nupp.kpp.server.model.Shipment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Модульні тести предметної логіки варіанта 21 (кур'єрські доставки).
 * Не потребують MySQL, Tomcat або JavaFX.
 */
class CourseworkTest {

    private static final double DELTA = 0.001;

    private final CustomerService customers = new CustomerService();
    private final ShipmentService shipments = new ShipmentService();
    private final DeliveryOrderService orders = new DeliveryOrderService();

    // ---------- нормування факторів ----------

    @Test
    void f1IsZeroForNonUrgentDelivery() {
        assertEquals(0.0, RiskCalculator.f1(24), DELTA);
        assertEquals(0.0, RiskCalculator.f1(48), DELTA);
    }

    @Test
    void f1IsFiftyForTwelveHourWindow() {
        assertEquals(50.0, RiskCalculator.f1(12), DELTA);
    }

    @Test
    void f1RejectsNonPositivePlannedTime() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.f1(0));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.f1(-5));
    }

    @Test
    void f2IsZeroWithoutDelay() {
        assertEquals(0.0, RiskCalculator.f2(4, 4), DELTA);
        assertEquals(0.0, RiskCalculator.f2(4, 2), DELTA);
    }

    @Test
    void f2IsFiftyWhenDelayIsHalfOfPlannedTime() {
        assertEquals(50.0, RiskCalculator.f2(4, 6), DELTA);
    }

    @Test
    void f2IsCappedAtHundred() {
        assertEquals(100.0, RiskCalculator.f2(2, 10), DELTA);
    }

    @Test
    void f3EqualsFragility() {
        assertEquals(85.0, RiskCalculator.f3(85), DELTA);
    }

    @Test
    void f3RejectsFragilityAbove100() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.f3(101));
    }

    // ---------- інтегральний показник ----------

    @Test
    void integralUsesWeights() {
        // 0,40·50 + 0,35·50 + 0,25·100 = 62,5
        assertEquals(62.5, RiskCalculator.integral(50, 50, 100), DELTA);
    }

    @Test
    void integralRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.integral(101, 10, 10));
    }

    @Test
    void integralWeightsSumToOne() {
        assertEquals(1.0, RiskCalculator.W1 + RiskCalculator.W2 + RiskCalculator.W3, 1e-9);
    }

    @Test
    void orderFactorsAreCalculatedFromOrderAndShipment() {
        DeliveryOrder in = new DeliveryOrder(1, 1, 1, null, OrderState.CREATED, 4, 6, 0, 0, 0, 0);
        DeliveryOrder out = DeliveryOrderService.withFactors(in, 85);
        assertEquals(83.33, out.f1(), DELTA);
        assertEquals(50.0, out.f2(), DELTA);
        assertEquals(85.0, out.f3(), DELTA);
        assertEquals(72.08, out.integralIndex(), DELTA);
    }

    // ---------- життєвий цикл ----------

    @Test
    void lifecycleAllowsSequentialTransitions() {
        assertTrue(OrderLifecycle.canTransit(OrderState.CREATED, OrderState.ASSIGNED));
        assertTrue(OrderLifecycle.canTransit(OrderState.ASSIGNED, OrderState.IN_TRANSIT));
        assertTrue(OrderLifecycle.canTransit(OrderState.IN_TRANSIT, OrderState.DELIVERED));
    }

    @Test
    void lifecycleRejectsSkippingStates() {
        assertFalse(OrderLifecycle.canTransit(OrderState.CREATED, OrderState.IN_TRANSIT));
        assertFalse(OrderLifecycle.canTransit(OrderState.CREATED, OrderState.DELIVERED));
        assertFalse(OrderLifecycle.canTransit(OrderState.ASSIGNED, OrderState.DELIVERED));
    }

    @Test
    void lifecycleRejectsBackwardAndSameState() {
        assertFalse(OrderLifecycle.canTransit(OrderState.DELIVERED, OrderState.IN_TRANSIT));
        assertFalse(OrderLifecycle.canTransit(OrderState.IN_TRANSIT, OrderState.ASSIGNED));
        assertFalse(OrderLifecycle.canTransit(OrderState.ASSIGNED, OrderState.ASSIGNED));
    }

    @Test
    void requireTransitionThrowsForForbiddenTransition() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderLifecycle.requireTransition(OrderState.CREATED, OrderState.DELIVERED));
    }

    // ---------- валідація сутностей ----------

    @Test
    void validCustomerPasses() {
        Customer c = new Customer(0, "Іваненко Олег", "+380501112233", "Полтава, вул. Соборності, 10");
        assertDoesNotThrow(() -> customers.validate(c));
    }

    @Test
    void customerRejectsEmptyName() {
        Customer c = new Customer(0, "", "+380501112233", "Полтава");
        assertThrows(IllegalArgumentException.class, () -> customers.validate(c));
    }

    @Test
    void customerRejectsInvalidPhone() {
        Customer c = new Customer(0, "Іваненко Олег", "abc", "Полтава");
        assertThrows(IllegalArgumentException.class, () -> customers.validate(c));
    }

    @Test
    void shipmentRejectsFragilityAbove100() {
        Shipment s = new Shipment(0, "Склянка", 0.8, "Магазин «Дім»", 120);
        assertThrows(IllegalArgumentException.class, () -> shipments.validate(s));
    }

    @Test
    void shipmentRejectsNonPositiveWeight() {
        Shipment s = new Shipment(0, "Книги", 0, "Книгарня", 20);
        assertThrows(IllegalArgumentException.class, () -> shipments.validate(s));
    }

    @Test
    void orderRejectsZeroPlannedHours() {
        DeliveryOrder o = new DeliveryOrder(0, 1, 1, null, OrderState.CREATED, 0, 0, 0, 0, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> orders.validate(o));
    }
}
