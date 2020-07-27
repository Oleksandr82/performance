package victor.training.performance;

public class APlusPlus {
    private static final Object MUTEX = new Object();
    private static Integer infected = 0;

    public static class ThreadA extends Thread {
        public void run() {
            for (int i = 0; i < 100_000; i++) {
                synchronized (MUTEX) {
//                    infected = new Integer(infected.intValue() + 1);
                    infected ++;
                }
            }
        }
    }
    public static class ThreadB extends Thread {
        public void run() {
            for (int i = 0; i < 100_000; i++) {
                synchronized (MUTEX) {
                    infected++;
                }
            }
        }
    }
    // TODO (bonus): ConcurrencyUtil.useCPU(1)

    public static void main(String[] args) throws InterruptedException {
        ThreadA threadA = new ThreadA();
        ThreadB threadB = new ThreadB();

        long t0 = System.currentTimeMillis();

        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();

        long t1 = System.currentTimeMillis();
        System.out.println("Total = " + infected);
        System.out.println("Took = " + (t1 - t0));
    }
}
