package ua.edu.nupp.kpp.server.model;

public record Shipment(long id, String description, double weightKg, String senderName, double fragility) {
}
