package org.konceptosociala.netpkt.warehouse;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Server {
    private static final String VALID_LOGIN = "admin";
    private static final String VALID_PASSWORD = "1234";

    public static void main(String[] args) throws IOException, SQLException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        WarehouseService service = new WarehouseService("warehouse.db");

        server.createContext("/login", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String[] parts = body.split("&");
            String login = null, password = null;
            for (String p : parts) {
                if (p.startsWith("login=")) login = p.substring(6);
                if (p.startsWith("password=")) password = p.substring(9);
            }

            if (VALID_LOGIN.equals(login) && VALID_PASSWORD.equals(password)) {
                String token = JwtUtil.generateToken(login);
                exchange.sendResponseHeaders(200, token.length());
                exchange.getResponseBody().write(token.getBytes(StandardCharsets.UTF_8));
            } else {
                exchange.sendResponseHeaders(401, -1);
            }
            exchange.close();
        });

        // /api/good/{id}
        server.createContext("/api/good", new GoodHandler(service));

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8080/");
    }

    static boolean checkAuth(HttpExchange exchange) throws IOException {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.sendResponseHeaders(403, -1);
            return false;
        }
        String token = authHeader.substring(7);
        if (!JwtUtil.isValid(token)) {
            exchange.sendResponseHeaders(403, -1);
            return false;
        }
        return true;
    }
}
