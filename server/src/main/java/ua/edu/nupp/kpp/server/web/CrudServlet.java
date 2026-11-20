package ua.edu.nupp.kpp.server.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.service.CrudService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP-контролер: приймає запит, викликає Service, повертає JSON і HTTP-статус.
 * GET — список або один запис (?id=), POST — створення, PUT — редагування, DELETE (?id=) — видалення.
 * Предметної логіки та SQL тут немає.
 */
public abstract class CrudServlet<T> extends HttpServlet {

    @FunctionalInterface
    protected interface Action {
        Object run() throws Exception;
    }

    private static final Logger LOG = Logger.getLogger(CrudServlet.class.getName());
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Class<T> type;

    protected CrudServlet(Class<T> type) {
        this.type = type;
    }

    protected abstract CrudService<T> crud();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, HttpServletResponse.SC_OK, () -> {
            String id = req.getParameter("id");
            if (id == null) {
                return crud().list();
            }
            return crud().get(parseId(id));
        });
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, HttpServletResponse.SC_CREATED, () -> crud().create(readBody(req, type)));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, HttpServletResponse.SC_OK, () -> crud().update(readBody(req, type)));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        handle(req, resp, HttpServletResponse.SC_OK, () -> {
            long id = parseId(req.getParameter("id"));
            crud().delete(id);
            return Map.of("deleted", id);
        });
    }

    /** Виконує дію та перетворює винятки на коректні HTTP-статуси. */
    protected void handle(HttpServletRequest req, HttpServletResponse resp, int okStatus, Action action)
            throws IOException {
        try {
            write(resp, okStatus, action.run());
        } catch (IllegalArgumentException e) {
            write(resp, HttpServletResponse.SC_BAD_REQUEST, error(e.getMessage()));
        } catch (JsonProcessingException e) {
            write(resp, HttpServletResponse.SC_BAD_REQUEST, error("Некоректний JSON у тілі запиту"));
        } catch (NoSuchElementException e) {
            write(resp, HttpServletResponse.SC_NOT_FOUND, error(e.getMessage()));
        } catch (SQLIntegrityConstraintViolationException e) {
            LOG.log(Level.WARNING, "Порушення цілісності даних: " + e.getMessage());
            write(resp, HttpServletResponse.SC_CONFLICT,
                    error("Операція неможлива: запис пов'язаний з іншими даними"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Помилка обробки " + req.getMethod() + " " + req.getRequestURI(), e);
            write(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error("Внутрішня помилка сервера"));
        }
    }

    protected <X> X readBody(HttpServletRequest req, Class<X> cls) throws IOException {
        req.setCharacterEncoding("UTF-8");
        return MAPPER.readValue(req.getReader(), cls);
    }

    protected static long parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Не вказано параметр id");
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некоректний id: " + raw);
        }
    }

    private static Map<String, String> error(String message) {
        return Map.of("error", message == null ? "Помилка запиту" : message);
    }

    private static void write(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        MAPPER.writeValue(resp.getWriter(), body);
    }
}
