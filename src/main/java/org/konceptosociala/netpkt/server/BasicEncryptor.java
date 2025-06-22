package org.konceptosociala.netpkt.server;

import org.konceptosociala.netpkt.packet.CryptoUtil;
import org.konceptosociala.netpkt.packet.Message;

public class BasicEncryptor implements Encryptor {
    public byte[] encrypt(Message message) throws Exception {
        return CryptoUtil.encrypt(message.toBytes());
    }
}
