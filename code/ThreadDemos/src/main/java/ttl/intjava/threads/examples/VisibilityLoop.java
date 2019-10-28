package ttl.intjava.threads.examples;

import java.util.concurrent.Phaser;

public class VisibilityLoop {

    private int var;
    private boolean ready;
    private Thread th1, th2;
    private boolean keepGoing = true;

    public static void main(String[] args) {
        // measureLoop(1000);

        VisibilityLoop ro = new VisibilityLoop();
        ro.go();


        System.out.println("All Done");

    }

    boolean firstTime = true;
    private int numReordered = 0;

    public void go() {

        final int numIterations = 1_000_000;

        var = 0;
        ready = false;
        keepGoing = true;
        numReordered = 0;
        Phaser phaser = new Phaser(2) {

            protected boolean onAdvance(int phase, int registeredParties) {

                //System.out.println("In OnAdvace with Thread " + Thread.currentThread().getName());
                if (var == 0) {
                    System.out.println("Worker1 var is " + var + ", ready = "
                            + ready + ", iter = " + phase);
                    numReordered++;
                }
                var = 0;
                ready = false;
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
            int i = 0;
            int iter = 0;
            System.out.println("Worker 1 Thread" + Thread.currentThread().getName());
            while (keepGoing) {

                while (!ready) {
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
            int i = 0;
            System.out.println("Worker 2 Thread" + Thread.currentThread().getName());
            while (keepGoing) {

                var = 10;
                ready = true;

                phaser.arriveAndAwaitAdvance();
            }

        }
    }

}
