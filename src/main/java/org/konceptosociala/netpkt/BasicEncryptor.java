package org.konceptosociala.netpkt;

public class BasicEncryptor implements Encryptor {
    public byte[] encrypt(Message message) throws Exception {
        return CryptoUtil.encrypt(message.toBytes());
    }
}
