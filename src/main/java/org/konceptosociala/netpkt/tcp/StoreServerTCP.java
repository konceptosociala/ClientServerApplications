package org.konceptosociala.netpkt.tcp;

import org.konceptosociala.netpkt.packet.*;
import org.konceptosociala.netpkt.server.*;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class StoreServerTCP {
    private static AtomicLong pktId = new AtomicLong(0);

    public static void main(String[] args) throws Exception {
        int port = 5555;
        
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Warehouse TCP Server started on port " + port);

            WarehouseProcessor warehouse = new WarehouseProcessor();

            while (true) {
                Socket client = server.accept();
                System.out.println("Client connected: " + client.getInetAddress());
                new Thread(() -> handleClient(client, warehouse)).start();
            }
        }
    }

    private static void handleClient(Socket client, WarehouseProcessor warehouse) {
        try (
            DataInputStream in = new DataInputStream(client.getInputStream());
            DataOutputStream out = new DataOutputStream(client.getOutputStream())
        ) {
            while (true) {
                if (client.isClosed() || !client.isConnected()) {
                    System.out.println("Client disconnected: " + client.getInetAddress());
                    break;
                }

                if (in.available() != 0) {
                    int length = in.readInt();
                    byte[] buffer = new byte[length];
                    in.readFully(buffer);
                    System.out.println("Received packet with " + length + " bytes from " + client.getInetAddress());

                    Packet pkt = Packet.fromBytes(buffer);
                    byte[] response = new PacketBuilder()
                        .msg(new Message(
                            -1,
                            -1,
                            warehouse.process(pkt.message)
                        ))
                        .bSrc((byte) 255)
                        .pktId(pktId.incrementAndGet())
                        .build();

                    out.writeInt(response.length);
                    out.write(response);
                    out.flush();
                }
            }
        } catch (Exception ex) {
            System.out.println("Failed to receive packet from " + client.getInetAddress());
        }
    }
}