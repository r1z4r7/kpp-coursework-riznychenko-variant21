package ua.edu.nupp.kpp.server.model;

/** Стани життєвого циклу замовлення доставки. */
public enum OrderState {
    CREATED,     // СТВОРЕНО
    ASSIGNED,    // ПРИЗНАЧЕНО
    IN_TRANSIT,  // В_ДОРОЗІ
    DELIVERED    // ДОСТАВЛЕНО
}
