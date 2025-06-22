package org.konceptosociala.netpkt.server;

import java.util.concurrent.*;

import org.konceptosociala.netpkt.packet.Message;

public class ServerApp {
    public static void main(String[] args) throws Exception {
        int msgCount = 100;
        BlockingQueue<byte[]> inQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Message> procQueue = new LinkedBlockingQueue<>();
        BlockingQueue<byte[]> outQueue = new LinkedBlockingQueue<>();

        Receiver receiver = new FakeReceiver(inQueue, msgCount);
        Decryptor Decryptor = new BasicDecryptor();
        Processor processor = new WarehouseProcessor();
        Encryptor Encryptor = new BasicEncryptor();
        Sender sender = new FakeSender();

        ExecutorService pool = Executors.newFixedThreadPool(8);

        // Потік прийому повідомлень
        pool.submit(receiver::receiveMessage);

        // Потік дешифрування
        pool.submit(() -> {
            try {
                while (true) {
                    byte[] data = inQueue.take();
                    Message msg = Decryptor.decrypt(data);
                    procQueue.put(msg);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Потік обробки
        pool.submit(() -> {
            try {
                while (true) {
                    Message msg = procQueue.take();
                    byte[] response = processor.process(msg);
                    outQueue.put(response);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Потік шифрування та відправки
        pool.submit(() -> {
            try {
                while (true) {
                    byte[] resp = outQueue.take();
                    Message dummyMsg = new Message();
                    dummyMsg.payload = resp;
                    byte[] enc = Encryptor.encrypt(dummyMsg);
                    sender.sendMessage(enc, null);
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // Завершення через певний час
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);
    }
}
