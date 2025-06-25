package org.konceptosociala.netpkt;

import org.konceptosociala.netpkt.packet.Message;
import org.konceptosociala.netpkt.packet.Packet;
import org.konceptosociala.netpkt.packet.PacketBuilder;

import junit.framework.TestCase;

public class PacketTest extends TestCase {

    public void testPacketBuildAndParse() throws Exception {
        Message msg = new Message();
        msg.cType = 42;
        msg.bUserId = 123;
        msg.payload = "{\"hello\":\"world\"}".getBytes();

        byte[] packetBytes = new PacketBuilder()
            .msg(msg)
            .bSrc((byte) 2)
            .pktId(1001L)
            .build();

        Packet pkt = Packet.fromBytes(packetBytes);

        assertEquals(42, pkt.message.cType);
        assertEquals(123, pkt.message.bUserId);
        assertEquals(new String(msg.payload), new String(pkt.message.payload));
    }
}