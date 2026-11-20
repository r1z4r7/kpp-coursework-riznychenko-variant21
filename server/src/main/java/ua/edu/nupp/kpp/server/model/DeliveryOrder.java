package ua.edu.nupp.kpp.server.model;

/** Замовлення доставки. courierId = null, доки кур'єра не призначено. */
public record DeliveryOrder(long id, long customerId, long shipmentId, Long courierId,
                            OrderState state, double plannedHours, double actualHours,
                            double f1, double f2, double f3, double integralIndex) {
}
