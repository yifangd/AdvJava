package ttl.intjava.threads.racecondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Program to illustrate a race condition. We create a bunch of threads which
 * are accessing a shared Repository of data. The threads spin in a loop and
 * increment a counter in the repository. We want the counter to always have a
 * coherent value, which is one more than it's last value. So, if we have 5
 * threads that spin around 50 times each, we want the final value of the
 * counter to be 249.
 * <p/>
 * Also demonstrate the use of java.util.concurrent.CountDownLatch.
 * 
 * @author Anil Pal Date: Apr 28, 2010 Time: 10:46:34 PM
 */
public class RaceConditionLatch extends Thread {

	private static final int numThreads = 5;
	// This holds each thread at the "start gate", waiting for it to
	// be "opened".
	private static CountDownLatch startGate = new CountDownLatch(1);
	// This is used by the main thread to wait for all other threads
	// to finish.
	private static CountDownLatch endGate = new CountDownLatch(numThreads);

	private final int repeat = 50;

	// The data
	private Repository repository;

	// Each thread stores the counter values it sees in a List
	private List<Integer> numbers = new ArrayList<Integer>();

	// To keep track of the number of Threads
	private static List<RaceConditionLatch> racers = new ArrayList<RaceConditionLatch>();

	public RaceConditionLatch(String name, Repository rep) {
		super(name);
		this.repository = rep;
	}

	public void run() {
		try {
			// Wait for the gate to be "opened", which is done below.
			// This is an attempt to have all threads start off more
			// or less together, all the better to make them cross
			// each others paths.
			startGate.await();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		// Spin in a loop and update the counter
		for (int i = 0; i < repeat; i++) {

			// Add the value you see to your own list of numbers
			int val = repository.getData();
			numbers.add(val);
			try {
				Thread.sleep(1);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			// And increment the value
			int result = ThreadLocalRandom.current().nextInt(1, 10);
			// And add the result into the value
			repository.setData(val + result);
		}

		// Let the endGate know you are finished.
		endGate.countDown();
	}

	public static void main(String[] args) {
		Repository rep = new Repository();
		for (int i = 0; i < numThreads; i++) {
			RaceConditionLatch rc = new RaceConditionLatch(i + 1 + "", rep);
			racers.add(rc);
			rc.start();
		}

		// "Open" the start gate and let the
		// racers start racing
		startGate.countDown();

		try {
			// And then wait for them all to be done
			endGate.await();
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		// Collect all the numbers into one list
		List<Integer> allNums = new ArrayList<Integer>();
		for (RaceConditionLatch rc : racers) {
			// System.out.println(rc.numbers);
			allNums.addAll(rc.numbers);
		}

		Collections.sort(allNums);

		System.out.println("All nums = " + allNums);

		// Look for duplicates. If all has gone well, there should
		// not be any duplicates.
		System.out.println("Duplicates: ");
		Integer last = null;
		int end = allNums.size();

		for (int i = 0; i < end; i++) {
			Integer curr = allNums.get(i);
			if (curr.equals(last)) {
				System.out.print(last + " ");
			}
			last = curr;
		}
	}
}
