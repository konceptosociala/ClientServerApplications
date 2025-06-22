package org.konceptosociala.netpkt.udp;

import java.net.*;

import org.konceptosociala.netpkt.packet.Message;
import org.konceptosociala.netpkt.packet.Packet;
import org.konceptosociala.netpkt.packet.PacketBuilder;

public class StoreServerUDP {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5556);
        byte[] buf = new byte[2048];

        System.out.println("UDP Server started on port 5556");
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            byte[] data = new byte[packet.getLength()];
            System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
            Packet req = Packet.fromBytes(data);

            // Обробити запит і надіслати відповідь
            byte[] response = PacketBuilder.build(
                new Message(/* відповідь */), req.bSrc, req.bPktId
            );
            DatagramPacket resp = new DatagramPacket(
                response, response.length,
                packet.getAddress(), packet.getPort()
            );
            socket.send(resp);
        }
    }
}
