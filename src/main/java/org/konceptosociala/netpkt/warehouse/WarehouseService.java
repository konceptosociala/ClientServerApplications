package org.konceptosociala.netpkt.warehouse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WarehouseService {
    private final String url;

    public WarehouseService(String dbFile) throws SQLException {
        this.url = "jdbc:sqlite:" + dbFile;
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement st = conn.createStatement()) {
                st.execute("""
                    CREATE TABLE IF NOT EXISTS items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        price REAL NOT NULL
                    )
                """);
            }
        }
    }

    public Item create(Item item) throws SQLException {
        String sql = "INSERT INTO items(name, category, quantity, price) VALUES (?, ?, ?, ?)";
        try (
            Connection conn = DriverManager.getConnection(url);
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getQuantity());
            ps.setDouble(4, item.getPrice());
            ps.executeUpdate();
            // SQLite JDBC driver does not fully support getGeneratedKeys for AUTOINCREMENT columns.
            // Instead, fetch the last inserted row id and return the created item.
            try (
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")
            ) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Item(id, item.getName(), item.getCategory(), item.getQuantity(), item.getPrice());
                }
            }
        }
        throw new SQLException("Failed to insert item");
    }

    public Item read(long id) throws SQLException {
        String sql = "SELECT id, name, category, quantity, price FROM items WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    );
                }
            }
        }
        return null;
    }

    public boolean update(long id, Item newData) throws SQLException {
        String sql = "UPDATE items SET name=?, category=?, quantity=?, price=? WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newData.getName());
            ps.setString(2, newData.getCategory());
            ps.setInt(3, newData.getQuantity());
            ps.setDouble(4, newData.getPrice());
            ps.setLong(5, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long id) throws SQLException {
        String sql = "DELETE FROM items WHERE id=?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Item> search(
            String name,
            String category,
            Integer minQty,
            Integer maxQty,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    ) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT id, name, category, quantity, price FROM items WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (name != null) {
            sb.append(" AND lower(name) LIKE ?");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (category != null) {
            sb.append(" AND lower(category) = ?");
            params.add(category.toLowerCase());
        }
        if (minQty != null) {
            sb.append(" AND quantity >= ?");
            params.add(minQty);
        }
        if (maxQty != null) {
            sb.append(" AND quantity <= ?");
            params.add(maxQty);
        }
        if (minPrice != null) {
            sb.append(" AND price >= ?");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sb.append(" AND price <= ?");
            params.add(maxPrice);
        }
        sb.append(" ORDER BY id ASC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            List<Item> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Item(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getDouble("price")
                    ));
                }
            }
            return result;
        }
    }

    public void clear() throws SQLException {
        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement()) {
            st.execute("DELETE FROM items");
            st.execute("DELETE FROM sqlite_sequence WHERE name='items'");
        }
    }
}