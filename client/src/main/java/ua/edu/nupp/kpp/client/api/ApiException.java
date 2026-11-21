package ua.edu.nupp.kpp.client.api;

/** Помилка звернення до сервера. network = true, якщо сервер недоступний. */
public class ApiException extends RuntimeException {

    private final int status;
    private final boolean network;

    public ApiException(String message, int status, boolean network) {
        super(message);
        this.status = status;
        this.network = network;
    }

    public int status() {
        return status;
    }

    public boolean isNetwork() {
        return network;
    }
}
