package org.konceptosociala.netpkt.udp;

import java.net.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.konceptosociala.netpkt.packet.*;

public class StoreClientUDP {
    private static AtomicLong pktId = new AtomicLong(0);

    private DatagramSocket socket;
    private InetAddress serverAddr;
    private int serverPort;

    public StoreClientUDP(String host, int port) throws Exception {
        socket = new DatagramSocket();
        serverAddr = InetAddress.getByName(host);
        serverPort = port;
        socket.setSoTimeout(3000); // 3 seconds timeout
    }

    public boolean sendPacket(byte[] pkt) {
        try {
            DatagramPacket dp = new DatagramPacket(pkt, pkt.length, serverAddr, serverPort);
            socket.send(dp);

            byte[] buf = new byte[2048];
            DatagramPacket resp = new DatagramPacket(buf, buf.length);
            socket.receive(resp);

            Packet answer = Packet.fromBytes(java.util.Arrays.copyOf(resp.getData(), resp.getLength()));
            System.out.println("Received response: " + new String(answer.message.payload));
            return true;
        } catch (Exception ex) {
            System.out.println("No response or error: " + ex.getMessage());
            return false;
        }
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        StoreClientUDP client = new StoreClientUDP("localhost", 5556);
        Random random = new Random();

        for (int i = 0; i < random.nextInt(1, 6); i++) {
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

            byte[] pkt = new PacketBuilder()
                .msg(msg)
                .bSrc((byte) random.nextInt(0, 255))
                .pktId(pktId.incrementAndGet())
                .build();

            client.sendPacket(pkt);
            Thread.sleep(250);
        }
        client.close();
    }
}
