package ua.edu.nupp.kpp.client.model;

public record Shipment(long id, String description, double weightKg, String senderName, double fragility) {
}
