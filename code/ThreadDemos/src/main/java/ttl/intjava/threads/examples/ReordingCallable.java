package ttl.intjava.threads.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ReordingCallable {

	private int X;
	private int Y;
	private int a;
	private int b;

	private static int totalReordered = 0;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// measureLoop(1000);

		ReordingCallable ro = new ReordingCallable();
		int numRounds = 10;
		ro.go(1);

	}

	public void go(int round) throws InterruptedException, ExecutionException {
		int numCores = Runtime.getRuntime().availableProcessors();
		ExecutorService es = Executors.newFixedThreadPool(numCores);

		Worker1 w1 = new Worker1();
		Worker2 w2 = new Worker2();
		
		BadWorker bw = new BadWorker();

		int numReordered = 0;
		int finalResult = 0;

		Future<Integer> f2 = es.submit(w2);
		Future<Integer> f1 = es.submit(w1);
		
		Future<?> f3 = es.submit(bw);

		finalResult += f1.get();
		finalResult += f2.get();

		System.out.println("finalResult is " + finalResult);

		//es.shutdownNow();
		es.shutdown();
		int count = 0;
		while(!es.isTerminated()) {
			es.awaitTermination(2, TimeUnit.SECONDS);
			if(count++ > 3) {
				//Log error
				break;
			}
			System.out.println("Shutdown after " + count);
		}
	}

	class Worker1 implements Callable<Integer> {

		private int sum = 0;

		public Integer call() {
			for (int i = 0; i < 1000; i++) {
				sum += i;
			}

			return sum;
		}
	}

	class Worker2 implements Callable<Integer> {

		private int sum = 0;

		public Integer call() {
			for (int i = 0; i < 1000; i++) {
				sum += i;
			}

			return sum;
		}
	}
	
	class BadWorker implements Runnable {
		public void run() {
			for(;!Thread.currentThread().isInterrupted();) {
				System.out.println("Bad Worker " + Thread.currentThread().getName() + " still here");
			}
		}
	}

}
