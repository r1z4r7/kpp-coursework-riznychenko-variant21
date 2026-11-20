package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.model.OrderState;

/** Правила переходів життєвого циклу: СТВОРЕНО → ПРИЗНАЧЕНО → В_ДОРОЗІ → ДОСТАВЛЕНО. */
public final class OrderLifecycle {

    private OrderLifecycle() {
    }

    public static boolean canTransit(OrderState from, OrderState to) {
        return (from == OrderState.CREATED && to == OrderState.ASSIGNED)
                || (from == OrderState.ASSIGNED && to == OrderState.IN_TRANSIT)
                || (from == OrderState.IN_TRANSIT && to == OrderState.DELIVERED);
    }

    public static void requireTransition(OrderState from, OrderState to) {
        if (!canTransit(from, to)) {
            throw new IllegalArgumentException("Перехід " + from + " → " + to + " не дозволений");
        }
    }
}
