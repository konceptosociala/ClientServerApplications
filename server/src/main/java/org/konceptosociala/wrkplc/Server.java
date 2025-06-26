package org.konceptosociala.wrkplc;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.*;

public class Server {
    private static final String UNAME = "root";
    private static final String PASSWD = "root";
    public static void main(String[] args){
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8211), 0);
            server.createContext("/", new WarehouseHttpHandler(UNAME, PASSWD));
            server.setExecutor(null);
            server.start();

            System.out.println("Server started on http://localhost:8211");
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}