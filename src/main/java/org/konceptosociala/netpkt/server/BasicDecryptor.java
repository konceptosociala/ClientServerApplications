package org.konceptosociala.netpkt.server;

import org.konceptosociala.netpkt.packet.CryptoUtil;
import org.konceptosociala.netpkt.packet.Message;

public class BasicDecryptor implements Decryptor {
    public Message decrypt(byte[] message) throws Exception {
        byte[] decrypted = CryptoUtil.decrypt(message);
        return Message.fromBytes(decrypted);
    }
}
