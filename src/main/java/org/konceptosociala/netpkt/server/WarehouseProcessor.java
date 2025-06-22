package org.konceptosociala.netpkt.server;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.konceptosociala.netpkt.packet.Message;

public class WarehouseProcessor implements Processor {
    private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();

    public byte[] process(Message message) throws Exception {
        String product = "Гречка"; 
        int code = message.cType;
        String response = "Ok";

        switch (code) {
            case 1: 
            int qty = stock.containsKey(product) ? stock.get(product).get() : 0;
            response = "{\"qty\":" + qty + "}";
            break;
            case 2: 
            stock.putIfAbsent(product, new AtomicInteger(0));
            int amtRemove = Integer.parseInt(new String(message.payload).replaceAll("\\D", ""));
            stock.get(product).addAndGet(-amtRemove);
            response = "Removed " + amtRemove;
            break;
            case 3: 
            stock.putIfAbsent(product, new AtomicInteger(0));
            int amtAdd = Integer.parseInt(new String(message.payload).replaceAll("\\D", ""));
            stock.get(product).addAndGet(amtAdd);
            response = "Added " + amtAdd;
            break;
            case 4: 
            String groupName = new String(message.payload).trim();
            response = "Group '" + groupName + "' added";
            break;
            case 5:
            String[] parts = new String(message.payload).split(":");
            if (parts.length == 2) {
                String grp = parts[0].trim();
                String prod = parts[1].trim();
                response = "Product '" + prod + "' added to group '" + grp + "'";
            } else {
                response = "Invalid payload";
            }
            break;
            case 6: 
            String[] priceParts = new String(message.payload).split(":");
            if (priceParts.length == 2) {
                String prod = priceParts[0].trim();
                String price = priceParts[1].trim();
                response = "Price for '" + prod + "' set to " + price;
            } else {
                response = "Invalid payload";
            }
            break;
            default:
            response = "Unknown command";
        }
        return response.getBytes();
    }
}