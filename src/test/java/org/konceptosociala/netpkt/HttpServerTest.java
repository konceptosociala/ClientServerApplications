package org.konceptosociala.netpkt;

import junit.framework.TestCase;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class HttpServerTest extends TestCase {
    private String token;

    @Override
    protected void setUp() throws Exception {
        token = login("admin", "1234");
        assertNotNull("Login failed, token is null", token);
    }

    public void testCreateAndReadGood() throws Exception {
        // PUT /api/good
        String json = """
            {"name": "Box", "category": "Storage", "quantity": 20, "price": 15.5}
            """;

        HttpURLConnection con = request("PUT", "/api/good", token);
        writeBody(con, json);

        assertEquals(201, con.getResponseCode());
        String resp = readResponse(con);
        assertTrue(resp.contains("\"id\":"));

        long id = Long.parseLong(resp.replaceAll("\\D+", ""));
        con.disconnect();

        // GET /api/good/{id}
        con = request("GET", "/api/good/" + id, token);
        assertEquals(200, con.getResponseCode());
        String body = readResponse(con);
        assertTrue(body.contains("Box"));
        con.disconnect();
    }

    public void testUpdateGood() throws Exception {
        long id = createGood("Chair", "Furniture", 5, 30.0);

        // POST /api/good/{id}
        String json = """
            {"name": "Chair Deluxe", "category": "Furniture", "quantity": 10, "price": 60.0}
            """;

        HttpURLConnection con = request("POST", "/api/good/" + id, token);
        writeBody(con, json);

        assertEquals(204, con.getResponseCode());
        con.disconnect();

        // Confirm update
        con = request("GET", "/api/good/" + id, token);
        String body = readResponse(con);
        assertTrue(body.contains("Chair Deluxe"));
        con.disconnect();
    }

    public void testDeleteGood() throws Exception {
        long id = createGood("Lamp", "Electronics", 3, 20.0);
        HttpURLConnection con = request("DELETE", "/api/good/" + id, token);
        assertEquals(204, con.getResponseCode());
        con.disconnect();

        con = request("GET", "/api/good/" + id, token);
        assertEquals(404, con.getResponseCode());
        con.disconnect();
    }

    // -------------------------------
    // 🔧 Helper Methods
    // -------------------------------

    private String login(String user, String pass) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8080/login").openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        writeBody(con, "login=" + user + "&password=" + pass);

        if (con.getResponseCode() != 200) return null;
        return readResponse(con);
    }

    private long createGood(String name, String cat, int qty, double price) throws Exception {
        String json = String.format(
            "{\"name\":\"%s\",\"category\":\"%s\",\"quantity\":%d,\"price\":%s}",
            name, cat, qty, price
        );
        HttpURLConnection con = request("PUT", "/api/good", token);
        writeBody(con, json);

        if (con.getResponseCode() != 201) throw new IOException("Failed to create good");
        String body = readResponse(con);
        con.disconnect();
        return Long.parseLong(body.replaceAll("\\D+", ""));
    }

    private HttpURLConnection request(String method, String path, String token) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:8080" + path).openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        return con;
    }

    private void writeBody(HttpURLConnection con, String data) throws IOException {
        try (OutputStream os = con.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readResponse(HttpURLConnection con) throws IOException {
        InputStream is = con.getResponseCode() >= 400 ? con.getErrorStream() : con.getInputStream();
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            return sb.toString();
        }
    }
}
