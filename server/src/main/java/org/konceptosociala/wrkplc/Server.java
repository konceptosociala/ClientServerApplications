package org.konceptosociala.wrkplc;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import com.sun.net.httpserver.*;

public class Server {
    private static final String UNAME = "root";
    private static final String PASSWD = "root";
    public static void main(String[] args){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8211), 0);
            HttpContext context = server.createContext("/", new AuthHandler());

            server.setExecutor(null); // Default executor
            server.start();

            System.out.println("Server started on http://localhost:8211");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Headers headers = exchange.getRequestHeaders();
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Access-Control-Allow-Origin", "*");

            String auth = headers.getFirst("Authorization");

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                responseHeaders.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                responseHeaders.set("Access-Control-Allow-Headers", "Authorization, Content-Type");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (auth == null || !isAuthorized(auth)) {
                responseHeaders.set("WWW-Authenticate", "Basic realm=\"MyServer\"");
                exchange.sendResponseHeaders(401, -1);
                return;
            }

            String response = "Hello, root!";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private boolean isAuthorized(String authHeader) {
            if (!authHeader.startsWith("Basic ")) return false;

            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);

            return parts.length == 2
                && UNAME.equals(parts[0])
                && PASSWD.equals(parts[1]);
        }
    }

}