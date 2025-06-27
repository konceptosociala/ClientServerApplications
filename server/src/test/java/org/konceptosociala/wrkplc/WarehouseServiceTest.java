package org.konceptosociala.wrkplc;

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;

public class WarehouseServiceTest extends TestCase {
    private WarehouseServiceImpl service;

    protected void setUp() {
        File dbFile = new File("test.db");
        if (dbFile.exists()) {
            dbFile.delete();
        }
        service = new WarehouseServiceImpl("jdbc:sqlite:test.db");
    }

    public void testAddAndGetGroup() {
        boolean added = service.addGroup("Продукти", "Продовольчі товари");
        assertTrue(added);

        JSONArray groups = service.getAllGroups();
        assertEquals(1, groups.length());
        assertEquals("Продукти", groups.getJSONObject(0).getString("name"));
    }

    public void testAddAndGetProduct() {
        service.addGroup("Господарські", "Для дому");
        boolean added = service.addProduct("Мило", "Господарське", "CleanCo", 10, 5.5, "Господарські");
        assertTrue(added);

        JSONArray products = service.getAllProducts("");
        assertEquals(1, products.length());
        JSONObject p = products.getJSONObject(0);
        assertEquals("Мило", p.getString("name"));
        assertEquals(10, p.getInt("quantity"));
    }

    public void testUpdateProduct() {
        service.addGroup("Хімія", "Побутова хімія");
        service.addProduct("Сода", "Харчова", "SodaCorp", 2, 7.0, "Хімія");

        JSONObject update = new JSONObject();
        update.put("quantity", 5);
        update.put("price", 8.0);

        boolean updated = service.updateProduct("Сода", update);
        assertTrue(updated);

        JSONArray products = service.getAllProducts("Сода");
        JSONObject p = products.getJSONObject(0);
        assertEquals(5, p.getInt("quantity"));
        assertEquals(8.0, p.getDouble("price"));
    }

    public void testDeleteProduct() {
        service.addGroup("Група", "Опис групи");
        service.addProduct("Оцет", "Столовий", "VinegarInc", 5, 3.5, "Група");

        boolean deleted = service.deleteProduct("Оцет");
        assertTrue(deleted);

        JSONArray products = service.getAllProducts("Оцет");
        assertEquals(0, products.length());
    }

    public void testCascadeDeleteGroup() {
        service.addGroup("Крупи", "Різні крупи");
        service.addProduct("Гречка", "Крупа", "GrainFarm", 50, 35.0, "Крупи");
        JSONArray products1 = service.getAllProducts("Гречка");
        assertEquals(1, products1.length());

        service.deleteGroup("Крупи");

        JSONArray products2 = service.getAllProducts("Гречка");
        assertEquals(0, products2.length());
    }
}
