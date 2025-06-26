package org.konceptosociala.wrkplc;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.*;

public class WarehouseServiceImpl implements WarehouseService {

    private final String dbUrl = "jdbc:sqlite:warehouse.db";

    public WarehouseServiceImpl() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS groups (
                    name TEXT PRIMARY KEY,
                    description TEXT
                );
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    name TEXT PRIMARY KEY,
                    description TEXT,
                    manufacturer TEXT,
                    quantity INTEGER,
                    price REAL,
                    group_name TEXT,
                    FOREIGN KEY (group_name) REFERENCES groups(name) ON DELETE CASCADE
                );
            """);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    @Override
    public JSONArray getAllGroups() {
        JSONArray arr = new JSONArray();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM groups");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                arr.put(new JSONObject()
                    .put("name", rs.getString("name"))
                    .put("description", rs.getString("description")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arr;
    }

    @Override
    public JSONArray getAllProducts(String search) {
        JSONArray arr = new JSONArray();
        String q = "SELECT * FROM products";
        boolean hasSearch = search != null && !search.isBlank();
        if (hasSearch) {
            q += " WHERE name LIKE ? OR description LIKE ? OR manufacturer LIKE ? OR group_name LIKE ?";
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(q)) {

            if (hasSearch) {
                for (int i = 1; i <= 4; i++) {
                    stmt.setString(i, "%" + search + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                arr.put(new JSONObject()
                    .put("name", rs.getString("name"))
                    .put("description", rs.getString("description"))
                    .put("manufacturer", rs.getString("manufacturer"))
                    .put("quantity", rs.getInt("quantity"))
                    .put("price", rs.getDouble("price"))
                    .put("group", rs.getString("group_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return arr;
    }

    @Override
    public boolean addGroup(String name, String description) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO groups (name, description) VALUES (?, ?)")) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // likely duplicate
        }
    }

    @Override
    public boolean deleteGroup(String name) {
        try (Connection conn = DriverManager.getConnection(dbUrl)) {
            conn.setAutoCommit(false);
            try (
                PreparedStatement deleteProducts = conn.prepareStatement("DELETE FROM products WHERE group_name = ?");
                PreparedStatement deleteGroup = conn.prepareStatement("DELETE FROM groups WHERE name = ?")
            ) {
                deleteProducts.setString(1, name);
                deleteProducts.executeUpdate();

                deleteGroup.setString(1, name);
                boolean result = deleteGroup.executeUpdate() > 0;

                conn.commit();
                return result;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean addProduct(String name, String description, String manufacturer, int quantity, double price, String group) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("""
                 INSERT INTO products (name, description, manufacturer, quantity, price, group_name)
                 VALUES (?, ?, ?, ?, ?, ?)
             """)) {
            stmt.setString(1, name);
            stmt.setString(2, description);
            stmt.setString(3, manufacturer);
            stmt.setInt(4, quantity);
            stmt.setDouble(5, price);
            stmt.setString(6, group);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean updateProduct(String name, JSONObject fields) {
        StringBuilder sql = new StringBuilder("UPDATE products SET ");
        boolean first = true;

        for (String key : fields.keySet()) {
            if (!first) sql.append(", ");
            switch (key) {
                case "description":
                    sql.append("description = ?");
                    break;
                case "manufacturer":
                    sql.append("manufacturer = ?");
                    break;
                case "quantity":
                    sql.append("quantity = ?");
                    break;
                case "price":
                    sql.append("price = ?");
                    break;
                case "group":
                    sql.append("group_name = ?");
                    break;
                default:
                    continue;
            }
            first = false;
        }

        sql.append(" WHERE name = ?");
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            for (String key : fields.keySet()) {
                switch (key) {
                    case "description" -> stmt.setString(i++, fields.getString(key));
                    case "manufacturer" -> stmt.setString(i++, fields.getString(key));
                    case "quantity" -> stmt.setInt(i++, fields.getInt(key));
                    case "price" -> stmt.setDouble(i++, fields.getDouble(key));
                    case "group" -> stmt.setString(i++, fields.getString(key));
                }
            }
            stmt.setString(i, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean deleteProduct(String name) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM products WHERE name = ?")) {
            stmt.setString(1, name);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
