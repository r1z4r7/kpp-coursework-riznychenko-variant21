# Курсова робота: інформаційна система керування кур'єрськими доставками

Дисципліна «Кросплатформене програмування», варіант 21.
Виконав: Різниченко Артем Андрійович, група 202-ТН.
Керівник: Заїка С. О.

## Короткий опис

Клієнт-серверна кросплатформена система мовою Java. JavaFX-клієнт обмінюється з сервером
через HTTP/JSON; сервер (Servlet API, шари Service і DAO) зберігає дані в MySQL.

Основні сутності: **Клієнт**, **Кур'єр**, **Відправлення**, **Замовлення доставки**.

- Життєвий цикл замовлення: `CREATED → ASSIGNED → IN_TRANSIT → DELIVERED`
  (СТВОРЕНО → ПРИЗНАЧЕНО → В_ДОРОЗІ → ДОСТАВЛЕНО). Недопустимі переходи сервер відхиляє;
  для переходу в `ASSIGNED` обов'язково вказується кур'єр.
- Інтегральний показник: `I = 0,40·F1 + 0,35·F2 + 0,25·F3`, усі фактори нормовані до 0–100:
  - F1 (рівень сервісу / терміновість) = `clamp((24 − planned) / 24 · 100, 0, 100)`, де `planned` — плановий час доставки, год;
  - F2 (величина затримки) = `clamp((actual − planned) / planned · 100, 0, 100)`;
  - F3 (чутливість вантажу) = поле `fragility` відправлення (0–100).
- Фоновий імпорт відправлень із CSV через JavaFX `Task` з індикатором прогресу.

## Технології

Java 21, Maven, JavaFX 21, Servlet API 4.0.1 (Apache Tomcat 9.x), MySQL / MariaDB (XAMPP),
MySQL Connector/J, Jackson, JUnit 5.

## Структура репозиторію

```
database.sql        створення бази courier_delivery і початкових даних
server/             серверна частина (WAR): model, dao, service, web
client/             JavaFX-клієнт
  sample-data/      приклади CSV для імпорту
```

Маршрут даних: `JavaFX → ApiClient → HTTP/JSON → Servlet → Service → DAO → JDBC → MySQL`.

## Вимоги до середовища

JDK 21, Apache Maven, MySQL (наприклад, з XAMPP), Apache Tomcat 9.x (не 10.x).

## Підготовка бази даних

1. Запустіть MySQL.
2. Виконайте `database.sql` (наприклад, phpMyAdmin → «Імпорт»). Скрипт створює базу `courier_delivery`
   з нуля, чотири таблиці та початкові записи.

Параметри підключення сервера за замовчуванням: `jdbc:mysql://localhost:3306/courier_delivery`,
користувач `root`, без пароля. Для іншого середовища задайте змінні середовища
`DB_URL`, `DB_USER`, `DB_PASSWORD`. Паролі в репозиторій не додаються.

## Складання й запуск сервера

```
cd server
mvn clean package
```

Скопіюйте `server/target/courier-server.war` у каталог `webapps` Apache Tomcat 9 і запустіть Tomcat
(`bin/catalina.bat run` або `bin/catalina.sh run`). Перевірка:
`http://localhost:8080/courier-server/customers` має повернути JSON-масив клієнтів.

Адреси сервера: `/customers`, `/couriers`, `/shipments`, `/orders`
(GET — список або `?id=`, POST — створення, PUT — редагування, DELETE `?id=` — видалення);
`POST /orders?action=transition&id=N` — зміна стану, `POST /orders?action=recalc&id=N` — перерахунок показника.

## Запуск JavaFX-клієнта

Сервер і MySQL мають працювати.

```
cd client
mvn javafx:run
```

Адреса сервера за замовчуванням `http://localhost:8080/courier-server`; змінюється змінною
середовища `COURIER_API_URL`. Журнал клієнта пишеться у `client/client.log`
(серверні повідомлення — у журнали Tomcat).

## Тести JUnit 5

```
cd server
mvn clean test      # 22 тести: фактори, інтегральний показник, життєвий цикл, валідація
cd ../client
mvn clean test      # 8 тестів розбору CSV
```

Тести не потребують MySQL, Tomcat або JavaFX.

## Формат CSV для імпорту відправлень

Кодування UTF-8, роздільник `;`, перший рядок — заголовок:

```
description;weight_kg;sender_name;fragility
Ноутбук;2.3;Магазин «Техно»;70
```

| Поле | Опис |
|---|---|
| description | опис відправлення, обов'язкове |
| weight_kg | вага, кг, число більше 0 |
| sender_name | відправник, обов'язкове |
| fragility | чутливість вантажу, число 0–100 |

Некоректні рядки пропускаються й вказуються в підсумку; відправлення, що вже є в базі, пропускаються
як дублікати. Приклади файлів: `client/sample-data/shipments.csv`, `shipments-invalid.csv`,
`shipments-wrong-header.csv`.

## Сценарій перевірки

1. Запустити MySQL, виконати `database.sql`, запустити Tomcat із `courier-server.war`.
2. Запустити клієнт (`mvn javafx:run`).
3. Створити замовлення (наприклад, відправлення «Склянка», план 4 год, факт 6 год): очікується
   F1 = 83,33, F2 = 50,00, F3 = 85,00, I = 72,08.
4. Призначити кур'єра і провести замовлення через `ASSIGNED → IN_TRANSIT → DELIVERED`;
   спроба недопустимого переходу (наприклад, `DELIVERED → IN_TRANSIT`) завершується помилкою.
5. Імпортувати `shipments.csv`, повторити імпорт (дублікати), імпортувати `shipments-invalid.csv`
   та `shipments-wrong-header.csv`.
6. Виконати `mvn clean test` у `server` і `client`.
