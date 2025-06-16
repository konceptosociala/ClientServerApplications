package org.konceptosociala.netpkt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Message {
    public int cType;
    public int bUserId;
    public byte[] payload;

    public static Message fromBytes(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        Message msg = new Message();
        msg.cType = buf.getInt();
        msg.bUserId = buf.getInt();
        msg.payload = new byte[data.length - 8];
        buf.get(msg.payload);
        return msg;
    }

    public byte[] toBytes() {
        ByteBuffer buf = ByteBuffer.allocate(8 + payload.length).order(ByteOrder.BIG_ENDIAN);
        buf.putInt(cType);
        buf.putInt(bUserId);
        buf.put(payload);
        return buf.array();
    }
}