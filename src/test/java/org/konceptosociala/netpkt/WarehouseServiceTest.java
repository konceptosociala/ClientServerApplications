package org.konceptosociala.netpkt;

import junit.framework.TestCase;
import java.sql.SQLException;
import java.util.List;

import org.konceptosociala.netpkt.warehouse.WarehouseService;
import org.konceptosociala.netpkt.warehouse.Item;

public class WarehouseServiceTest extends TestCase {
    WarehouseService service;

    @Override
    protected void setUp() throws Exception {
        service = new WarehouseService("mydb.db");
        service.clear();
    }

    @Override
    protected void tearDown() throws Exception {
        service.clear();
    }

    public void testCreateAndRead() throws SQLException {
        Item item = service.create(new Item(0, "Banana", "Fruit", 30, 9.99));
        assertNotNull(item);
        assertEquals("Banana", item.getName());

        Item found = service.read(item.getId());
        assertEquals(item.getName(), found.getName());
        assertEquals(item.getCategory(), found.getCategory());
    }

    public void testUpdate() throws SQLException {
        Item item = service.create(new Item(0, "Bread", "Bakery", 50, 2.5));
        Item update = new Item(0, "Bread (Rye)", "Bakery", 60, 3.0);
        boolean ok = service.update(item.getId(), update);
        assertTrue(ok);

        Item found = service.read(item.getId());
        assertEquals("Bread (Rye)", found.getName());
        assertEquals(60, found.getQuantity());
        assertEquals(3.0, found.getPrice());
    }

    public void testDelete() throws SQLException {
        Item item = service.create(new Item(0, "Sugar", "Grocery", 100, 1.2));
        boolean deleted = service.delete(item.getId());
        assertTrue(deleted);
        assertNull(service.read(item.getId()));
    }

    public void testSearchByName() throws SQLException {
        service.create(new Item(0, "Apple", "Fruit", 10, 5.0));
        service.create(new Item(0, "Banana", "Fruit", 20, 3.0));
        service.create(new Item(0, "Bread", "Bakery", 5, 2.0));

        List<Item> found = service.search("ap", null, null, null, null, null, 0, 10);
        assertEquals(1, found.size());
        assertEquals("Apple", found.get(0).getName());
    }

    public void testSearchByCategoryAndPrice() throws SQLException {
        service.create(new Item(0, "Apple", "Fruit", 10, 5.0));
        service.create(new Item(0, "Banana", "Fruit", 20, 3.0));
        service.create(new Item(0, "Bread", "Bakery", 5, 2.0));

        List<Item> found = service.search(null, "Fruit", null, null, 4.0, null, 0, 10);
        assertEquals(1, found.size());
        assertEquals("Apple", found.get(0).getName());
    }

    public void testSearchByQuantityRangeAndPagination() throws SQLException {
        for (int i = 1; i <= 25; i++) {
            service.create(new Item(0, "Item" + i, "Category", i, 1.0 * i));
        }
        List<Item> found = service.search(null, null, 10, 20, null, null, 0, 5);
        assertEquals(5, found.size());
        assertEquals("Item10", found.get(0).getName());
        assertEquals("Item14", found.get(4).getName());

        List<Item> page2 = service.search(null, null, 10, 20, null, null, 1, 5);
        assertEquals(5, page2.size());
        assertEquals("Item15", page2.get(0).getName());
    }
}