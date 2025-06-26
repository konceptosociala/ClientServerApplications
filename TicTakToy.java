public class TicTakToy {
    private static final int REPEAT_COUNT = 5;
    private static final Object lock = new Object();
    private static int turn = 0; // 0 = Tic, 1 = Tak, 2 = Toy

    public static void main(String[] args) {
        Thread tic = new Thread(() -> {
            for (int i = 0; i < REPEAT_COUNT; i++) {
                synchronized (lock) {
                    while (turn != 0) {
                        try { lock.wait(); } catch (InterruptedException ignored) {}
                    }
                    System.out.print("Tic-");
                    turn = 1;
                    lock.notifyAll();
                }
            }
        });

        Thread tak = new Thread(() -> {
            for (int i = 0; i < REPEAT_COUNT; i++) {
                synchronized (lock) {
                    while (turn != 1) {
                        try { lock.wait(); } catch (InterruptedException ignored) {}
                    }
                    System.out.print("Tak");
                    turn = 2;
                    lock.notifyAll();
                }
            }
        });

        Thread toy = new Thread(() -> {
            for (int i = 0; i < REPEAT_COUNT; i++) {
                synchronized (lock) {
                    while (turn != 2) {
                        try { lock.wait(); } catch (InterruptedException ignored) {}
                    }
                    System.out.println("-Toy");
                    turn = 0;
                    lock.notifyAll();
                }
            }
        });

        tic.start();
        tak.start();
        toy.start();

        try {
            tic.join();
            tak.join();
            toy.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}