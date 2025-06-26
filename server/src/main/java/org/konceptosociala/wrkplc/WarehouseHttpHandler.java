package org.konceptosociala.wrkplc;

import com.sun.net.httpserver.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.stream.Collectors;

public class WarehouseHttpHandler implements HttpHandler {
    private final String username;
    private final String password;
    private final WarehouseService service;

    public WarehouseHttpHandler(String username, String password) {
        this.service = new WarehouseServiceImpl();
        this.username = username;
        this.password = password;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            Headers responseHeaders = exchange.getResponseHeaders();

            // CORS
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            responseHeaders.add("Access-Control-Allow-Headers", "Authorization, Content-Type");

            if (method.equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Basic Auth check
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !isAuthorized(auth)) {
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"Warehouse\"");
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            // ROUTING
            if (path.equals("/auth") && method.equals("GET")) {
                handleAuth(exchange);
            } else if (path.equals("/groups") && method.equals("GET")) {
                handleGetGroups(exchange);
            } else if (path.equals("/products") && method.equals("GET")) {
                handleGetProducts(exchange);
            } else if (path.equals("/groups") && method.equals("POST")) {
                handleAddGroup(exchange);
            } else if (path.startsWith("/groups/") && method.equals("DELETE")) {
                handleDeleteGroup(exchange);
            } else if (path.equals("/products") && method.equals("POST")) {
                handleAddProduct(exchange);
            } else if (path.startsWith("/products/") && method.equals("PUT")) {
                handleUpdateProduct(exchange);
            } else if (path.startsWith("/products/") && method.equals("DELETE")) {
                handleDeleteProduct(exchange);
            } else {
                sendJSON(exchange, 404, new JSONObject().put("error", "Not Found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendJSON(exchange, 500, new JSONObject().put("error", "Internal Server Error"));
        }
    }

    private void handleAuth(HttpExchange exchange) throws IOException {
        String response = "Authorized successfully";
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private void handleAddProduct(HttpExchange exchange) throws IOException {
        JSONObject body = readBody(exchange);

        String name = body.optString("name");
        String description = body.optString("description", "");
        String manufacturer = body.optString("manufacturer", "");
        int quantity = body.optInt("quantity", 0);
        double price = body.optDouble("price", 0.0);
        String group = body.optString("group");

        if (name.isBlank() || group.isBlank()) {
            sendJSON(exchange, 400, new JSONObject().put("error", "Name and group are required"));
            return;
        }

        boolean success = service.addProduct(name, description, manufacturer, quantity, price, group);
        if (success) {
            sendJSON(exchange, 201, new JSONObject().put("message", "Product added"));
        } else {
            sendJSON(exchange, 409, new JSONObject().put("error", "Product name must be unique"));
        }
    }

    private void handleUpdateProduct(HttpExchange exchange) throws IOException {
        String name = exchange.getRequestURI().getPath().substring("/products/".length());
        JSONObject body = readBody(exchange);

        boolean success = service.updateProduct(name, body);
        if (success) {
            sendJSON(exchange, 200, new JSONObject().put("message", "Product updated"));
        } else {
            sendJSON(exchange, 404, new JSONObject().put("error", "Product not found"));
        }
    }

    private boolean isAuthorized(String authHeader) {
        if (!authHeader.startsWith("Basic ")) return false;

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        String credentials = new String(Base64.getDecoder().decode(base64Credentials));
        String[] parts = credentials.split(":", 2);

        return parts.length == 2
            && username.equals(parts[0])
            && password.equals(parts[1]);
    }

    private void handleGetGroups(HttpExchange exchange) throws IOException {
        JSONArray groups = service.getAllGroups(); // Stub: implement in DB service
        sendJSON(exchange, 200, groups);
    }

    private void handleDeleteProduct(HttpExchange exchange) throws IOException {
        String name = exchange.getRequestURI().getPath().substring("/products/".length());

        boolean success = service.deleteProduct(name);
        if (success) {
            sendJSON(exchange, 200, new JSONObject().put("message", "Product deleted"));
        } else {
            sendJSON(exchange, 404, new JSONObject().put("error", "Product not found"));
        }
    }

    private void handleGetProducts(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String search = null;
        if (query != null && query.startsWith("search=")) {
            search = query.substring(7);
        }
        JSONArray products = service.getAllProducts(search); // Implement search in service
        sendJSON(exchange, 200, products);
    }

    private void handleAddGroup(HttpExchange exchange) throws IOException {
        JSONObject body = readBody(exchange);
        String name = body.optString("name", null);
        String description = body.optString("description", "");

        if (name == null || name.isBlank()) {
            sendJSON(exchange, 400, new JSONObject().put("error", "Group name required"));
            return;
        }

        boolean success = service.addGroup(name, description);
        if (success) {
            sendJSON(exchange, 201, new JSONObject().put("message", "Group added"));
        } else {
            sendJSON(exchange, 409, new JSONObject().put("error", "Group already exists"));
        }
    }

    private void handleDeleteGroup(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String name = path.substring("/groups/".length());
        boolean success = service.deleteGroup(name);
        if (success) {
            sendJSON(exchange, 200, new JSONObject().put("message", "Group deleted"));
        } else {
            sendJSON(exchange, 404, new JSONObject().put("error", "Group not found"));
        }
    }

    // --- Utilities ---
    private JSONObject readBody(HttpExchange exchange) throws IOException {
        String body;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            body = reader.lines().collect(Collectors.joining("\n"));
        }
        return new JSONObject(body);
    }

    private void sendJSON(HttpExchange exchange, int code, JSONObject obj) throws IOException {
        byte[] response = obj.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }

    private void sendJSON(HttpExchange exchange, int code, JSONArray array) throws IOException {
        byte[] response = array.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}

