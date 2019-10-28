package ttl.intjava.threads.examples;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadSpecificData {

	// Atomic integer containing the next thread ID to be assigned
	private static final AtomicInteger nextId = new AtomicInteger(0);

	final int x = 35;

	// Thread local variable containing each thread's ID
	private static final ThreadLocal<Integer> threadId = new ThreadLocal<Integer>() {
		@Override
		protected Integer initialValue() {
			return nextId.getAndIncrement();
		}
	};

	// Returns the current thread's unique ID, assigning it if necessary
	public static int get() {
		return threadId.get();
	}

	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			Thread th1 = new Thread(() -> System.out.println("Thread Id is " + get()));
			th1.start();
		}
	}
}
