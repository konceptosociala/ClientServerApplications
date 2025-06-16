package org.konceptosociala.netpkt;

import java.net.InetAddress;

public class FakeSender implements Sender {
    public void sendMessage(byte[] message, InetAddress target) {
        System.out.println("Відправлено: " + new String(message));
    }
}
