package org.konceptosociala.netpkt;

import junit.framework.TestCase;

import java.util.concurrent.*;

public class ServerAppTest extends TestCase {
    public void testMultithreadedProcessing() throws Exception {
        int msgCount = 50;
        BlockingQueue<byte[]> inQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> procQueue = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>();

        Receiver receiver = new FakeReceiver(inQueue, msgCount);
        Decryptor Decryptor = new BasicDecryptor();
        Processor processor = new WarehouseProcessor();
        Encryptor Encryptor = new BasicEncryptor();
        Sender sender = new FakeSender();

        ExecutorService pool = Executors.newFixedThreadPool(8);

        pool.submit(receiver::receiveMessage);

        pool.submit(() -> {
            try {
                while (true) {
                    byte[] data = inQueue.poll(2, TimeUnit.SECONDS);
                    if (data == null) break;
                    Message msg = Decryptor.decrypt(data);
                    procQueue.put(msg);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        pool.submit(() -> {
            try {
                while (true) {
                    Message msg = procQueue.poll(2, TimeUnit.SECONDS);
                    if (msg == null) break;
                    byte[] response = processor.process(msg);
                    outQueue.put(response);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        pool.submit(() -> {
            try {
                while (true) {
                    byte[] resp = outQueue.poll(2, TimeUnit.SECONDS);
                    if (resp == null) break;
                    Message dummyMsg = new Message();
                    dummyMsg.payload = resp;
                    byte[] enc = Encryptor.encrypt(dummyMsg);
                    sender.sendMessage(enc, null);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        assertTrue(pool.isTerminated());
    }
}