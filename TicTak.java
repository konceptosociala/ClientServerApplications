public class TicTak {
    private static final int REPEAT_COUNT = 5;
    private static final Object lock = new Object();
    private static int turn = 0; // 0 = Tic, 1 = Tak

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
                    System.out.println("Tak");
                    turn = 0;
                    lock.notifyAll();
                }
            }
        });

        tic.start();
        tak.start();

        try {
            tic.join();
            tak.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}