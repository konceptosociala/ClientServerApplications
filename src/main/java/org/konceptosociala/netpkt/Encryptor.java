package org.konceptosociala.netpkt;

public interface Encryptor {
    byte[] encrypt(Message message) throws Exception;
}
