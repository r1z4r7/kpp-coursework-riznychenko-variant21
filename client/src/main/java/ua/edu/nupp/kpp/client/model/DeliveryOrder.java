package ua.edu.nupp.kpp.client.model;

public record DeliveryOrder(long id, long customerId, long shipmentId, Long courierId,
                            OrderState state, double plannedHours, double actualHours,
                            double f1, double f2, double f3, double integralIndex) {
}
