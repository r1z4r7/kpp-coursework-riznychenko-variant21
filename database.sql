DROP DATABASE IF EXISTS courier_delivery;
CREATE DATABASE courier_delivery CHARACTER SET utf8mb4;
USE courier_delivery;

CREATE TABLE customer (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(160) NOT NULL,
    phone   VARCHAR(30)  NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE courier (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(160) NOT NULL,
    phone        VARCHAR(30)  NOT NULL,
    vehicle_type VARCHAR(60)  NOT NULL
);

CREATE TABLE shipment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    weight_kg   DOUBLE       NOT NULL,
    sender_name VARCHAR(160) NOT NULL,
    fragility   DOUBLE       NOT NULL   -- чутливість вантажу, 0..100 (F3)
);

CREATE TABLE delivery_order (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id    BIGINT      NOT NULL,
    shipment_id    BIGINT      NOT NULL,
    courier_id     BIGINT      NULL,           -- NULL до призначення кур'єра
    state          VARCHAR(30) NOT NULL,
    planned_hours  DOUBLE      NOT NULL,
    actual_hours   DOUBLE      NOT NULL,
    f1             DOUBLE      NOT NULL,
    f2             DOUBLE      NOT NULL,
    f3             DOUBLE      NOT NULL,
    integral_index DOUBLE      NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(id),
    FOREIGN KEY (shipment_id) REFERENCES shipment(id),
    FOREIGN KEY (courier_id)  REFERENCES courier(id)
);

INSERT INTO customer (name, phone, address) VALUES
('Іваненко Олег', '+380501112233', 'Полтава, вул. Соборності, 10'),
('Коваль Марія',  '+380672223344', 'Полтава, вул. Європейська, 5');

INSERT INTO courier (name, phone, vehicle_type) VALUES
('Петренко Андрій', '+380931234567', 'Велосипед'),
('Сидоренко Ігор',  '+380661234567', 'Автомобіль');

INSERT INTO shipment (description, weight_kg, sender_name, fragility) VALUES
('Книги',  2.5, 'Книгарня «Склад»', 20),
('Склянка', 0.8, 'Магазин «Дім»',   85);