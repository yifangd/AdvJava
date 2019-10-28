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
public class CallableWorkers {


	public static void main(String[] args) {
		CallableWorkers cw = new CallableWorkers();
		cw.go();

		cw.stop();
	}

	private ExecutorService executor;

	public CallableWorkers() {
		// Create a fixed thread pool ExecutorService
		int numCpus = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(numCpus);
	}

	public void go() {
		CallableWorker w1 = new CallableWorker();
		CallableWorker w2 = new CallableWorker();
		BadWorker w3 = new BadWorker();

		Future<Integer> f1 = executor.submit(w1);
		Future<Integer> f2 = executor.submit(w2);
		Future<?> f3 = executor.submit(w3);

		// Wait for them to finish
		int finalResult = 0;
		try {
			finalResult += f1.get();
			finalResult += f2.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		System.out.println("Final Result is " + finalResult);
	}

	/**
	 * Shutdown the ExecutorService
	 */
	public void stop() {
		//Shutdown nicely
		executor.shutdown();

		int tryLimit = 5;
		int numTries = 0;
		//Spin for upto tryLimit times waiting for it to stop
		while (!executor.isTerminated()) {
			if(++numTries > tryLimit) {
				//Something is very wrong.  Scream.
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
		public Integer call() {
			int sum = 0;
			for (int i = 0; i < 1000; i++) {
				sum += i;
			}

			return sum;
		}
	}

	/**
	 * A Bad Worker.  This one stays in a loop and doesn't listen for 
	 * Interruption.  There is NO way to kill this Thread.
	 * 
	 * @author whynot
	 *
	 */
	class BadWorker implements Runnable {
		public void run() {
			int sum = 1;
			for (int i = 0; i <= sum ; i++) {
				sum += i;
				i = sum - 1;
			}
		}
	}
}
