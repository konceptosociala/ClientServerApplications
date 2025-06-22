package org.konceptosociala.netpkt.tcp;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.konceptosociala.netpkt.packet.*;

public class StoreClientTCP {
    private static AtomicLong pktId = new AtomicLong(0);

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public static void main(String[] args) throws Exception {
        StoreClientTCP client = new StoreClientTCP();
        Random random = new Random();

        while (!client.connect("localhost", 5555)) {
            System.out.println("Waiting for server...");
            Thread.sleep(3000);
        }

        for (int i = 0; i < 999; i++) {
            Message msg = new Message();
            msg.cType = random.nextInt(6) + 1; 
            msg.bUserId = random.nextInt(5) + 1;
            switch (msg.cType) {
                case 1:
                    msg.payload = "Гречка".getBytes();
                    break;
                case 2:
                    msg.payload = ("Гречка:" + (random.nextInt(5) + 1)).getBytes();
                    break;
                case 3:
                    msg.payload = ("Гречка:" + (random.nextInt(10) + 1)).getBytes();
                    break;
                case 4:
                    msg.payload = ("Група" + (random.nextInt(3) + 1)).getBytes();
                    break;
                case 5:
                    msg.payload = ("Група" + (random.nextInt(3) + 1) + ":Гречка").getBytes();
                    break;
                case 6:
                    msg.payload = ("Гречка:" + (10 + random.nextInt(90)) + ".0").getBytes();
                    break;
                default:
                    msg.payload = "{}".getBytes();
            }

            while (!client.sendPacket(
                new PacketBuilder()
                    .msg(msg)
                    .bSrc((byte) random.nextInt(0, 255))
                    .pktId(pktId.incrementAndGet())
                    .build()
                )
            ) {
                if (!client.connect("localhost", 5555)) {
                    System.out.println("Waiting for send...");
                    Thread.sleep(3000);
                }
            }

            Packet response = null;
            for (int attempt = 0; attempt < 3 && response == null; attempt++) {
                try {
                    response = client.receivePacket();
                } catch (Exception e) {
                    System.out.println("Waiting for response...");
                    Thread.sleep(3000);
                }
            }

            if (response != null) {
                System.out.println("Received response: " + new String(response.message.payload));
            } else {
                System.out.println("No response received after 3 attempts");
            }
        }
        
        client.close();
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            socket.setSoTimeout(3000); // 3 seconds timeout
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean sendPacket(byte[] pkt) {
        try {
            out.write(ByteBuffer.allocate(4).putInt(pkt.length).array());
            out.write(pkt);
            out.flush();
            System.out.println("Sent packet with " + pkt.length + " bytes");
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public Packet receivePacket() throws Exception {
        byte[] lenBytes = in.readNBytes(4);
        if (lenBytes.length < 4) throw new IOException("No response");
        int len = ByteBuffer.wrap(lenBytes).getInt();
        byte[] pkt = in.readNBytes(len);
        return Packet.fromBytes(pkt);
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}