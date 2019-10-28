package ttl.intjava.threads.interrupt;

import java.util.Scanner;

public class InterruptDelay {
	private Object syncO = new Object();
	
	public static void main(String[] args) {
		InterruptDelay id = new InterruptDelay();
		
		id.go();
	}

	public void go() {
		Worker1 w1 = new Worker1("Worker 1");
		Thread th1 = new Thread(w1, "Worker 1");

		th1.start();

		Scanner scanner = new Scanner(System.in);

		System.out.print("Enter to send interrupt: ");
		scanner.nextLine();

		th1.interrupt();

		System.out.print("Enter to set waitForIt: ");
		w1.waitForIt = true;
		
		try {
			th1.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public class Worker1 implements Runnable {
		private String name;
		public volatile boolean waitForIt = false;

		public Worker1(String name) {
			this.name = name;
		}

		public void run() {
			System.out.println(name + " is going waiting for It");
			while (!waitForIt) {
			}

			synchronized (syncO) {
				System.out.println(name + " going to Wait on syncO");
				try {
					syncO.wait();
				} catch (InterruptedException e) {
					System.out.println(name + " got Interrupted in wait");
				}
			}
		}
	}
}
