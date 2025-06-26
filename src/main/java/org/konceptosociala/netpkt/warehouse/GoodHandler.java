package org.konceptosociala.netpkt.warehouse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoodHandler implements HttpHandler {
    private final WarehouseService service;
    private final Pattern idPattern = Pattern.compile("/api/good/(\\d+)");

    public GoodHandler(WarehouseService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!Server.checkAuth(exchange)) return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        Matcher m = idPattern.matcher(path);

        try {
            if (m.matches()) {
                long id = Long.parseLong(m.group(1));
                switch (method) {
                    case "GET" -> handleGet(exchange, id);
                    case "POST" -> handlePost(exchange, id);
                    case "DELETE" -> handleDelete(exchange, id);
                    default -> exchange.sendResponseHeaders(405, -1);
                }
            } else if ("/api/good".equals(path) && "PUT".equals(method)) {
                handlePut(exchange);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange, long id) throws IOException, SQLException {
        Item item = service.read(id);
        if (item == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        JSONObject json = new JSONObject();
        json.put("id", item.getId());
        json.put("name", item.getName());
        json.put("category", item.getCategory());
        json.put("quantity", item.getQuantity());
        json.put("price", item.getPrice());

        byte[] resp = json.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, resp.length);
        exchange.getResponseBody().write(resp);
    }

    private void handlePut(HttpExchange exchange) throws IOException, SQLException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);

        double price = json.optDouble("price", 0);
        if (price < 0) {
            exchange.sendResponseHeaders(409, -1);
            return;
        }

        Item item = new Item(0, json.getString("name"), json.getString("category"),
                json.getInt("quantity"), price);

        Item created = service.create(item);
        byte[] resp = ("{\"id\":" + created.getId() + "}").getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, resp.length);
        exchange.getResponseBody().write(resp);
    }

    private void handlePost(HttpExchange exchange, long id) throws IOException, SQLException {
        Item existing = service.read(id);
        if (existing == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject json = new JSONObject(body);
        double price = json.optDouble("price", 0);
        if (price < 0) {
            exchange.sendResponseHeaders(409, -1);
            return;
        }

        Item updated = new Item(id, json.getString("name"), json.getString("category"),
                json.getInt("quantity"), price);

        service.update(id, updated);
        exchange.sendResponseHeaders(204, -1);
    }

    private void handleDelete(HttpExchange exchange, long id) throws SQLException, IOException {
        if (!service.delete(id)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }
        exchange.sendResponseHeaders(204, -1);
    }
}
