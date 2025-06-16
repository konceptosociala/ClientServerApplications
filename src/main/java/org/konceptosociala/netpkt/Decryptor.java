package org.konceptosociala.netpkt;

public interface Decryptor {
    Message decrypt(byte[] message) throws Exception;
}
