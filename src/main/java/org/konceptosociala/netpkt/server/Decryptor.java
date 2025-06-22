package org.konceptosociala.netpkt.server;

import org.konceptosociala.netpkt.packet.Message;

public interface Decryptor {
    Message decrypt(byte[] message) throws Exception;
}
