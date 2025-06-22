package org.konceptosociala.netpkt.packet;

public class CRC16 {
    public static short crc16(byte[] bytes) {
        int crc = 0xFFFF;
        for (byte b : bytes) {
            crc ^= (b & 0xFF);
            for (int i = 0; i < 8; i++) {
                if ((crc & 1) != 0)
                    crc = (crc >>> 1) ^ 0xA001;
                else
                    crc = (crc >>> 1);
            }
        }
        return (short)crc;
    }
}