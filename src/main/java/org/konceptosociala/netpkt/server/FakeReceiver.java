package org.konceptosociala.netpkt.server;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

import org.konceptosociala.netpkt.packet.CryptoUtil;
import org.konceptosociala.netpkt.packet.Message;

public class FakeReceiver implements Receiver {
    private final BlockingQueue<byte[]> queue;
    private final int numMessages;
    private final Random rnd = new Random();

    public FakeReceiver(BlockingQueue<byte[]> queue, int numMessages) {
        this.queue = queue;
        this.numMessages = numMessages;
    }

    public void receiveMessage() {
        for (int i = 0; i < numMessages; i++) {
            try {
                Message msg = new Message();
                msg.cType = rnd.nextInt(6) + 1; 
                msg.bUserId = rnd.nextInt(5) + 1;
                switch (msg.cType) {
                    case 1:
                        msg.payload = "Гречка".getBytes();
                        break;
                    case 2:
                        msg.payload = ("Гречка:" + (rnd.nextInt(5) + 1)).getBytes();
                        break;
                    case 3:
                        msg.payload = ("Гречка:" + (rnd.nextInt(10) + 1)).getBytes();
                        break;
                    case 4:
                        msg.payload = ("Група" + (rnd.nextInt(3) + 1)).getBytes();
                        break;
                    case 5:
                        msg.payload = ("Група" + (rnd.nextInt(3) + 1) + ":Гречка").getBytes();
                        break;
                    case 6:
                        msg.payload = ("Гречка:" + (10 + rnd.nextInt(90)) + ".0").getBytes();
                        break;
                    default:
                        msg.payload = "{}".getBytes();
                }

                byte[] encrypted = CryptoUtil.encrypt(msg.toBytes());
                queue.put(encrypted);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
