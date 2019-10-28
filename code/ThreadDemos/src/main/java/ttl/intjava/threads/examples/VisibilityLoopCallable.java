package ttl.intjava.threads.examples;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class VisibilityLoopCallable {

	private int var;
	private boolean ready;
	private AtomicInteger counter = new AtomicInteger(0);

	ExecutorService es = Executors.newFixedThreadPool(4, (r) -> {
		Thread th = new Thread(r, "Worker " + counter.getAndIncrement());
		return th;
	});

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// measureLoop(1000);

		VisibilityLoopCallable ro = new VisibilityLoopCallable();
		ro.go();

		System.out.println("All Done");

		ro.es.shutdown();
	}

	boolean firstTime = true;

	int numReordered = 0;
	public void go() throws InterruptedException, ExecutionException {

		//final int numIterations = 1_000;
		final int numIterations = 1_000_000;

		var = 0;
		ready = false;
		numReordered = 0;

		Worker1 w1 = new Worker1();
		Worker2 w2 = new Worker2();

		for (int i = 0; i < numIterations; i++) {
			if(i % 1000 == 0) {
				System.out.println("Did " + i);
			}
			var = 0;
			ready = false;

			Future<?> f1 = es.submit(w1);
			Future<?> f2 = es.submit(w2);

			//get the second one first so we know that it finished
			f2.get();
			
			//This thread will get stuck here eventually
			f1.get();
		}
		System.out.printf("Found %d reorders in %d iterations (%.2f%%) %n", numReordered, numIterations,
				numReordered * 100. / numIterations);

	}

	class Worker1 implements Callable<Void> {

		public Worker1() {
		}

		public Void call() {

			while (!ready) {
			}

			if(var == 0) {
				numReordered++;
				System.out.println("Worker1 var is " + var);
			}

			return null;

		}
	}

	class Worker2 implements Callable<Void> {

		public Worker2() {
		}

		public Void call() {
			var = 10;
			ready = true;

			return null;
		}
	}
}
