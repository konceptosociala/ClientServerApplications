package org.konceptosociala.netpkt;

public interface Processor {
    byte[] process(Message message) throws Exception;
}