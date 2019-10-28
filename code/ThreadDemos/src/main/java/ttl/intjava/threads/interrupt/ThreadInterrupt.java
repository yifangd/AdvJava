package ttl.intjava.threads.interrupt;

import java.io.*;

class TestThread implements Runnable {
	int i = 0;

	public void run() {
		String threadName = Thread.currentThread().getName();
		synchronized (this) {
			while (true) {
				try {
					System.out.println("Thread " + threadName + ": going to wait" +
					", interrupt flag is " + Thread.currentThread().isInterrupted());
					this.wait();
					System.out.println("Thread " + threadName + ": normal exit from wait");
				} catch (InterruptedException ie) {
					System.out.println("Thread " +  threadName + ": Exceptional exit from wait"
							+ " Ex interrupt flag is " + Thread.currentThread().isInterrupted());
					
				}
				double j[] = new double[10000];

				for (int i = 0; i < j.length; i++) {
					j[i] = i * i / 2999.88945;
				}
				System.out.println("In Thread, j[length] =  " + j[j.length - 1]);
			}
		}
	}
}

public class ThreadInterrupt {
	public static void main(String[] args) throws Exception {
		TestThread tt = new TestThread();
		Thread thread = new Thread(tt);
		thread.start();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while (true) {
			System.out.print("Enter n for notify, i for interrupt :");
			line = br.readLine();

			if (line.equals("n")) {
				synchronized (tt) {
					tt.notify();
				}
			}
			else {
				thread.interrupt();
			}
		}
	}
}
