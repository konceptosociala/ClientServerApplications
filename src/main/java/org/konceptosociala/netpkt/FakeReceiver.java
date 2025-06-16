package org.konceptosociala.netpkt;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

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
                // Генерувати випадковий пакет (наприклад, "запит складу")
                Message msg = new Message();
                msg.cType = rnd.nextInt(6) + 1; // 1..6 типи команд
                msg.bUserId = rnd.nextInt(5) + 1;
                msg.payload = ("{\"product\":\"Гречка\", \"amount\":" + rnd.nextInt(10) + "}").getBytes();

                byte[] encrypted = CryptoUtil.encrypt(msg.toBytes());
                queue.put(encrypted);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
