package org.konceptosociala.wrkplc;

import org.json.JSONArray;
import org.json.JSONObject;

public interface WarehouseService {
    JSONArray getAllGroups();
    JSONArray getAllProducts(String search);
    boolean addGroup(String name, String description);
    boolean deleteGroup(String name);
    public boolean addProduct(String name, String description, String manufacturer, int quantity, double price, String group);
    public boolean updateProduct(String name, JSONObject fields);
    public boolean deleteProduct(String name);
}
