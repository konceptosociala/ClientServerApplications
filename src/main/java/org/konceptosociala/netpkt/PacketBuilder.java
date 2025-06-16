package org.konceptosociala.netpkt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PacketBuilder {
    public static byte[] build(Message msg, byte bSrc, long pktId) throws Exception {
        byte[] msgBytes = msg.toBytes();
        byte[] encryptedMsg = CryptoUtil.encrypt(msgBytes);

        int wLen = encryptedMsg.length;
        ByteBuffer buf = ByteBuffer.allocate(16 + wLen + 2).order(ByteOrder.BIG_ENDIAN);

        buf.put(Packet.MAGIC);
        buf.put(bSrc);
        buf.putLong(pktId);
        buf.putInt(wLen);

        // CRC16 header (00-13)
        byte[] headerBytes = new byte[14];
        buf.position(0);
        buf.get(headerBytes, 0, 14);
        short crcHeader = CRC16.crc16(headerBytes);
        buf.putShort(crcHeader);

        buf.put(encryptedMsg);

        // CRC16 message (16 до 16+wLen-1)
        short crcMsg = CRC16.crc16(encryptedMsg);
        buf.putShort(crcMsg);

        return buf.array();
    }
}