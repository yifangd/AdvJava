package ttl.intjava.threads.examples;

import java.util.concurrent.Phaser;

/**
 * Here we are checking the rule on Intel that 
 * older loads to *different* locations (i.e. X = 1 and Y = 1) can be Reordered
 * with newer reads (i.e. a = Y and b = X).  Thus, a == 0 && b == 0 is 
 * possible.  Need to use volatile to fix this issue.
 * 
 * See ReorderingSameLocation to see an example of a different scenario
 * in which reordering will not happen.
 * @author whynot
 *
 */
public class Reordering {

	private int X;
	private int Y;
	private int a, b;
	private Thread th1, th2;
	private static int totReordered = 0;
	private volatile boolean keepGoing = true;

	public static void main(String[] args) {

		Reordering ro = new Reordering();
		for (int i = 0; i < 10; i++) {
			ro.go();
		}
		System.out.println("All Done, totReordered = " + totReordered);

	}

	public void go() {

		final int numIterations = 100_000;

		X = Y = a = b = 0;
		keepGoing = true;
		Phaser phaser = new Phaser(2) {
			private int numReordered = 0;

			protected boolean onAdvance(int phase, int registeredParties) {
				if (a == 0 && b == 0) {
					numReordered++;
				}
				X = Y = a = b = 0;

				if (phase >= numIterations) {
					keepGoing = false;
					totReordered += numReordered;

					System.out
							.printf("Found %d reorders in %d iterations (%.2f%%), "
									+ "phase %d, %n",
									numReordered, numIterations, numReordered
											* 100. / numIterations, phase);
					return true;
				}
				return false;
			}
		};

		Worker1 w1 = new Worker1(phaser);
		Worker2 w2 = new Worker2(phaser);

		th1 = new Thread(w1);
		th2 = new Thread(w2);

		th1.start();
		th2.start();

		try {
			th1.join();
			th2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	class Worker1 implements Runnable {

		private Phaser phaser;

		public Worker1(Phaser phaser) {
			this.phaser = phaser;
		}

		public void run() {
			while (keepGoing) {
				X = 1;
				a = Y;

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
			while (keepGoing) {
				Y = 1;
				b = X;

				phaser.arriveAndAwaitAdvance();
			}
		}
	}

}
