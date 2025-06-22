package org.konceptosociala.netpkt.server;

import org.konceptosociala.netpkt.packet.Message;

public interface Encryptor {
    byte[] encrypt(Message message) throws Exception;
}
