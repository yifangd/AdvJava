package ttl.intjava.threads.prodcon;

public class ThreadState {

	public static void main(String [] args) throws InterruptedException {
		ThreadState ts = new ThreadState();
		ts.start();
		
		//ts.testThread.join();
		for(;;) {
			Thread.sleep(500);
			Thread.State state = ts.testThread.getState();
			System.out.println("ts.state = " + state);
			if(state == Thread.State.TERMINATED) {
				break;
			}
		}
	}
	
	public Thread testThread;
	public ThreadState() {
		testThread = new TestThread();
	}
	
	public void start() {
		testThread.start();
	}
	
	public class TestThread extends Thread
	{
		public void run() {
			try {
				System.out.println("TestThread going to sleep");
				Thread.sleep(2000);
			}
			catch(InterruptedException ex) {}
		}
	}
}
