package org.konceptosociala.netpkt.udp;

import java.net.*;
import java.util.Arrays;

import org.konceptosociala.netpkt.packet.*;

public class StoreClientUDP {
    private DatagramSocket socket;
    private InetAddress serverAddr;
    private int serverPort;

    public StoreClientUDP(String host, int port) throws Exception {
        socket = new DatagramSocket();
        serverAddr = InetAddress.getByName(host);
        serverPort = port;
        socket.setSoTimeout(1000); // 1 секунда на відповідь
    }

    public boolean sendPacketWithRetry(Packet pkt, int retries) throws Exception {
        byte[] pktBytes = PacketBuilder.build(pkt.message, pkt.bSrc, pkt.bPktId);
        DatagramPacket dp = new DatagramPacket(pktBytes, pktBytes.length, serverAddr, serverPort);

        for (int i = 0; i < retries; i++) {
            socket.send(dp);
            try {
                byte[] buf = new byte[2048];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);
                Packet answer = Packet.fromBytes(Arrays.copyOf(resp.getData(), resp.getLength()));
                System.out.println("Response: " + new String(answer.message.payload));
                return true;
            } catch (SocketTimeoutException ex) {
                System.out.println("No response, retrying " + (i+1));
            }
        }
        System.out.println("Failed after retries");
        return false;
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        StoreClientUDP client = new StoreClientUDP("localhost", 5556);

        for (int i = 0; i < 5; i++) {
            Message msg = new Message();
            msg.cType = 1;
            msg.bUserId = 1;
            msg.payload = "test".getBytes();
            Packet pkt = new Packet();
            pkt.message = msg;
            pkt.bSrc = 1;
            pkt.bPktId = i;

            client.sendPacketWithRetry(pkt, 3);
        }
        client.close();
    }
}
