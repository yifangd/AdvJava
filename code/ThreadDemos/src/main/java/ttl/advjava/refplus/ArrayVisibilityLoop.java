package ttl.advjava.refplus;

import java.util.concurrent.Phaser;

public class ArrayVisibilityLoop {

    private int var;
    private int arr [] = new int[10];
    private Thread th1, th2;
    private boolean keepGoing = true;

    public static void main(String[] args) {
        // measureLoop(1000);

        ArrayVisibilityLoop ro = new ArrayVisibilityLoop();
        ro.go();


        System.out.println("All Done");

    }

    private int numReordered = 0;

    public void go() {

        final int numIterations = 1_000_000;

        var = 0;
        arr = new int[100_000_000];
        keepGoing = true;
        numReordered = 0;
        Phaser phaser = new Phaser(2) {

            protected boolean onAdvance(int phase, int registeredParties) {

                //System.out.println("In OnAdvace with Thread " + Thread.currentThread().getName());
                if (var == 0) {
                    System.out.println("Worker1 var is " + var + ", arr[2000] = "
                            + arr[2000] + ", iter = " + phase);
                    numReordered++;
                }
                var = 0;
                arr[2000] = 0;
                if (phase >= numIterations) {
                    keepGoing = false;

                    System.out
                            .printf("Found %d reorders in %d iterations (%.2f%%), "
                                            + "phase %d%n",
                                    numReordered, numIterations, numReordered
                                            * 100. / numIterations, phase);

                    return true;
                }
                return false;
            }
        };

        Worker1 w1 = new Worker1(phaser);
        Worker2 w2 = new Worker2(phaser);

        th1 = new Thread(w1, "Worker1");
        th2 = new Thread(w2, "Worker2");

        th1.start();
        th2.start();

    }


    class Worker1 implements Runnable {

        private Phaser phaser;

        public Worker1(Phaser phaser) {
            this.phaser = phaser;
        }

        public void run() {
            System.out.println("Worker 1 Thread" + Thread.currentThread().getName());
            while (keepGoing) {

                while (arr[2000] != 10) {
                }

                System.out.println("Worker 1 var is " + var);

                phaser.arriveAndAwaitAdvance();


            }

        }
    }

    class Worker2 implements Runnable {

        private Phaser phaser;

        public Worker2(Phaser barrier) {
            this.phaser = barrier;
        }

        public void run() {
            System.out.println("Worker 2 Thread" + Thread.currentThread().getName());
            while (keepGoing) {

                var = 100;
                arr[2000] = 10;

                phaser.arriveAndAwaitAdvance();
            }

        }
    }

}
