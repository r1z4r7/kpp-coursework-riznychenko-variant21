package ua.edu.nupp.kpp.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.client.model.Courier;
import ua.edu.nupp.kpp.client.model.Customer;
import ua.edu.nupp.kpp.client.model.DeliveryOrder;
import ua.edu.nupp.kpp.client.model.OrderState;
import ua.edu.nupp.kpp.client.model.Shipment;
import ua.edu.nupp.kpp.client.model.TransitionRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * HTTP/JSON-клієнт до серверної частини. JavaFX працює лише з цим класом
 * і ніколи не звертається до MySQL напряму.
 * Адреса сервера береться зі змінної середовища COURIER_API_URL.
 */
public class ApiClient {

    private static final Logger LOG = Logger.getLogger(ApiClient.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final String baseUrl;

    public ApiClient() {
        this(defaultBaseUrl());
    }

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    private static String defaultBaseUrl() {
        String value = System.getenv("COURIER_API_URL");
        return (value == null || value.isBlank()) ? "http://localhost:8080/courier-server" : value;
    }

    // ---------- клієнти ----------
    public List<Customer> customers() {
        return list("/customers", Customer[].class);
    }

    public Customer addCustomer(Customer c) {
        return call("POST", "/customers", c, Customer.class);
    }

    public Customer updateCustomer(Customer c) {
        return call("PUT", "/customers", c, Customer.class);
    }

    public void deleteCustomer(long id) {
        call("DELETE", "/customers?id=" + id, null, Void.class);
    }

    // ---------- кур'єри ----------
    public List<Courier> couriers() {
        return list("/couriers", Courier[].class);
    }

    public Courier addCourier(Courier c) {
        return call("POST", "/couriers", c, Courier.class);
    }

    public Courier updateCourier(Courier c) {
        return call("PUT", "/couriers", c, Courier.class);
    }

    public void deleteCourier(long id) {
        call("DELETE", "/couriers?id=" + id, null, Void.class);
    }

    // ---------- відправлення ----------
    public List<Shipment> shipments() {
        return list("/shipments", Shipment[].class);
    }

    public Shipment addShipment(Shipment s) {
        return call("POST", "/shipments", s, Shipment.class);
    }

    public Shipment updateShipment(Shipment s) {
        return call("PUT", "/shipments", s, Shipment.class);
    }

    public void deleteShipment(long id) {
        call("DELETE", "/shipments?id=" + id, null, Void.class);
    }

    // ---------- замовлення доставки ----------
    public List<DeliveryOrder> orders() {
        return list("/orders", DeliveryOrder[].class);
    }

    public DeliveryOrder addOrder(DeliveryOrder o) {
        return call("POST", "/orders", o, DeliveryOrder.class);
    }

    public DeliveryOrder updateOrder(DeliveryOrder o) {
        return call("PUT", "/orders", o, DeliveryOrder.class);
    }

    public void deleteOrder(long id) {
        call("DELETE", "/orders?id=" + id, null, Void.class);
    }

    /** Зміна стану; courierId потрібен лише для переходу в ASSIGNED. */
    public DeliveryOrder transition(long id, OrderState to, Long courierId) {
        return call("POST", "/orders?action=transition&id=" + id,
                new TransitionRequest(to, courierId), DeliveryOrder.class);
    }

    public DeliveryOrder recalculate(long id) {
        return call("POST", "/orders?action=recalc&id=" + id, null, DeliveryOrder.class);
    }

    // ---------- внутрішня реалізація ----------
    private <T> List<T> list(String path, Class<T[]> arrayType) {
        return Arrays.asList(call("GET", path, null, arrayType));
    }

    private <T> T call(String method, String path, Object body, Class<T> type) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json");
        try {
            if (body == null) {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            } else {
                builder.header("Content-Type", "application/json; charset=UTF-8")
                        .method(method, HttpRequest.BodyPublishers.ofString(
                                MAPPER.writeValueAsString(body), StandardCharsets.UTF_8));
            }
            HttpResponse<String> response = http.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new ApiException(errorMessage(response.body(), status), status, false);
            }
            if (type == Void.class) {
                return null;
            }
            return MAPPER.readValue(response.body(), type);
        } catch (JsonProcessingException e) {
            LOG.warning("Некоректний JSON від сервера: " + e.getMessage());
            throw new ApiException("Сервер повернув некоректну відповідь", 0, false);
        } catch (IOException e) {
            LOG.warning("Сервер недоступний (" + method + " " + path + "): " + e.getMessage());
            throw new ApiException("Сервер недоступний. Перевірте, що запущені Tomcat і MySQL ("
                    + baseUrl + ")", 0, true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Запит перервано", 0, false);
        }
    }

    private static String errorMessage(String body, int status) {
        try {
            JsonNode node = MAPPER.readTree(body);
            if (node != null && node.hasNonNull("error")) {
                return node.get("error").asText();
            }
        } catch (JsonProcessingException ignored) {
            // тіло відповіді не є JSON — повертаємо загальне повідомлення
        }
        return "Помилка сервера (HTTP " + status + ")";
    }
}
