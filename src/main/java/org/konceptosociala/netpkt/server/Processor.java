package org.konceptosociala.netpkt.server;

import org.konceptosociala.netpkt.packet.Message;

public interface Processor {
    byte[] process(Message message) throws Exception;
}