package org.konceptosociala.netpkt;

import junit.framework.TestCase;

public class PacketTest extends TestCase {

    public void testPacketBuildAndParse() throws Exception {
        Message msg = new Message();
        msg.cType = 42;
        msg.bUserId = 123;
        msg.payload = "{\"hello\":\"world\"}".getBytes();

        byte[] packetBytes = PacketBuilder.build(msg, (byte)2, 1001L);

        Packet pkt = Packet.fromBytes(packetBytes);

        assertEquals(42, pkt.message.cType);
        assertEquals(123, pkt.message.bUserId);
        assertEquals(new String(msg.payload), new String(pkt.message.payload));
    }
}