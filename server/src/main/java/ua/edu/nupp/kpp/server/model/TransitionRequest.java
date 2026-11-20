package ua.edu.nupp.kpp.server.model;

/** Тіло запиту на зміну стану замовлення. courierId потрібен лише для переходу в ASSIGNED. */
public record TransitionRequest(OrderState state, Long courierId) {
}
