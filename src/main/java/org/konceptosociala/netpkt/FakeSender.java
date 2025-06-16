package org.konceptosociala.netpkt;

import java.net.InetAddress;
import java.util.Arrays;

public class FakeSender implements Sender {
    public void sendMessage(byte[] message, InetAddress target) {
        try {
            Message msg = Message.fromBytes(CryptoUtil.decrypt(message));
            System.out.println("Відправлено: " + new String(msg.payload));
        } catch (Exception e) {
            System.out.println("Відправлено (raw): " + Arrays.toString(message));
        }
    }
}
