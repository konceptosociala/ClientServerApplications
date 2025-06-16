package org.konceptosociala.netpkt;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class WarehouseProcessor implements Processor {
    private final ConcurrentHashMap<String, AtomicInteger> stock = new ConcurrentHashMap<>();

    public byte[] process(Message message) throws Exception {
        String product = "Гречка"; // Для прикладу, або парсити з payload
        int code = message.cType;
        String response = "Ok";

        switch (code) {
            case 1: // Get stock
                int qty = stock.containsKey(product) ? stock.get(product).get() : 0;
                response = "{\"qty\":" + qty + "}";
                break;
            case 2: // Remove from stock
                stock.putIfAbsent(product, new AtomicInteger(0));
                int amt = Integer.parseInt(new String(message.payload).replaceAll("\\D", ""));
                stock.get(product).addAndGet(-amt);
                break;
            case 3: // Add to stock
                stock.putIfAbsent(product, new AtomicInteger(0));
                amt = Integer.parseInt(new String(message.payload).replaceAll("\\D", ""));
                stock.get(product).addAndGet(amt);
                break;
            // ... додайте інші команди
            default:
                response = "Unknown command";
        }
        return response.getBytes();
    }
}