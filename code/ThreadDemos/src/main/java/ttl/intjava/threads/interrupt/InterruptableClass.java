package ttl.intjava.threads.interrupt;

import java.util.Scanner;

public class InterruptableClass implements Runnable {

	public void run() {
		double i = 10;
		while (true) {
			if(Thread.currentThread().isInterrupted()) {
				break;
			}
			i = i  * Math.sin(i);
			
			System.out.println("i is + " + i);
		}
	}
	
	public static void main(String[] args) {
		InterruptableClass ic = new InterruptableClass();
		Thread th = new Thread(ic);
		th.start();
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter to send interrupt ");
		String line = scanner.nextLine();
		scanner.close();
		th.interrupt();
	}
}
