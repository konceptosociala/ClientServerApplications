package org.konceptosociala.netpkt;

public class BasicDecryptor implements Decryptor {
    public Message decrypt(byte[] message) throws Exception {
        byte[] decrypted = CryptoUtil.decrypt(message);
        return Message.fromBytes(decrypted);
    }
}
