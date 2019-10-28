package ttl.intjava.threads.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Demo of ExecutorService and Callable
 * 
 * Create some Worker Threads by implementing Callable. Have them do dome
 * calculation, then collect all the results using Future.get
 * 
 * @author whynot
 *
 */
public class AtomicityFailure {


	public static void main(String[] args) throws InterruptedException {
		AtomicityFailure cw = new AtomicityFailure();

		for (int i = 0; i < 20; i++) {
			cw.go();

			Thread.sleep(1000);
		}

		cw.stop();
	}

	private ExecutorService executor;

	public AtomicityFailure() {
		// Create a fixed thread pool ExecutorService
		int numCpus = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(numCpus);
	}

	public void go() {
		IdGen idGen = new IdGen();

		CallableWorker w1 = new CallableWorker(idGen);
		CallableWorker w2 = new CallableWorker(idGen);

		Future<Integer> f1 = executor.submit(w1);
		Future<Integer> f2 = executor.submit(w2);

		// Wait for them to finish
		int finalResult = 0;
		try {
			finalResult += f1.get();
			finalResult += f2.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.err.println("At end nextId is " + idGen.getNextId());
	}

	/**
	 * Shutdown the ExecutorService
	 */
	public void stop() {
		// Shutdown nicely
		executor.shutdown();

		int tryLimit = 5;
		int numTries = 0;
		// Spin for upto tryLimit times waiting for it to stop
		while (!executor.isTerminated()) {
			if (++numTries > tryLimit) {
				// Something is very wrong. Scream.
				System.err.println("Executor Service not terminated after " + numTries + " tries");
				return;
			}
			try {
				executor.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Use a Callable class to create a task Each Worker will do some calculation
	 * and return a Result
	 * 
	 * @author whynot
	 *
	 */
	class CallableWorker implements Callable<Integer> {
		private IdGen idGen;

		public CallableWorker(IdGen idGen) {
			this.idGen = idGen;
		}

		public Integer call() {
			int sum = 0;
			for (int i = 0; i < 1000; i++) {
				int id = idGen.getNextId();
				sum += id;
			}

			return sum;
		}
	}
}

//TODO Need to properly synchronize this to make it work
class IdGen {
	private int nextId = 0;
	public int getNextId() {
			return ++nextId;
	}

}
