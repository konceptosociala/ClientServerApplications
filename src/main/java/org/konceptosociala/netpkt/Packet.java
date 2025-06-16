package org.konceptosociala.netpkt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Packet {
    public static final byte MAGIC = 0x13;
    public byte bMagic;
    public byte bSrc;
    public long bPktId;
    public int wLen;
    public short wCrc16;
    public byte[] bMsg;
    public short wCrc16Msg;

    public Message message;

    public static Packet fromBytes(byte[] data) throws Exception {
        if (data.length < 18) throw new IllegalArgumentException("Packet too short");
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        Packet pkt = new Packet();
        pkt.bMagic = buf.get();
        if (pkt.bMagic != MAGIC) throw new IllegalArgumentException("Wrong magic byte");

        pkt.bSrc = buf.get();
        pkt.bPktId = buf.getLong();
        pkt.wLen = buf.getInt();
        pkt.wCrc16 = buf.getShort();
        pkt.bMsg = new byte[pkt.wLen];
        buf.get(pkt.bMsg);

        pkt.wCrc16Msg = buf.getShort();

        short crcHeader = CRC16.crc16(Arrays.copyOfRange(data, 0, 14));
        if (crcHeader != pkt.wCrc16) throw new Exception("Header CRC16 invalid");

        short crcMsg = CRC16.crc16(Arrays.copyOfRange(data, 16, 16 + pkt.wLen));
        if (crcMsg != pkt.wCrc16Msg) throw new Exception("Message CRC16 invalid");

        byte[] decryptedMsg = CryptoUtil.decrypt(pkt.bMsg);

        pkt.message = Message.fromBytes(decryptedMsg);

        return pkt;
    }
}