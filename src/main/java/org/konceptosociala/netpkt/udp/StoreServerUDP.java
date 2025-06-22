package org.konceptosociala.netpkt.udp;

import org.konceptosociala.netpkt.packet.*;
import org.konceptosociala.netpkt.server.*;

import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class StoreServerUDP {
    private static AtomicLong pktId = new AtomicLong(0);

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5556);
        byte[] buf = new byte[2048];
        WarehouseProcessor warehouse = new WarehouseProcessor();

        System.out.println("Warehouse UDP Server started on port 5556");
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);

            Packet req = Packet.fromBytes(data);

            System.out.println("Received packet with " + data.length + " bytes from " + packet.getAddress());

            byte[] response = new PacketBuilder()
                .msg(new Message(
                    -1,
                    -1,
                    warehouse.process(req.message)
                ))
                .bSrc((byte) 255)
                .pktId(pktId.incrementAndGet())
                .build();

            DatagramPacket resp = new DatagramPacket(
                response, response.length,
                packet.getAddress(), packet.getPort()
            );
            socket.send(resp);
        }
    }
}
