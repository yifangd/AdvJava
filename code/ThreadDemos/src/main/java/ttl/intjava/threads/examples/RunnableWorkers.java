package ttl.intjava.threads.examples;

/**
 * Demo of the prodcon Thread api.
 * 
 * Create some Worker Threads by implementing Runnable.
 * Have them do dome calculation, then collect all the
 * results using  Thread.join
 * 
 * A very (very) simple form of Map/Reduce
 * @author whynot
 *
 */
public class RunnableWorkers {

	public static void main(String[] args) {
		new RunnableWorkers().go();
		//new RunnableWorkers().simpleGo();
	}
	
	public void simpleGo() {
		SimpleWorker sw = new SimpleWorker();
		Thread th = new Thread(sw);
		
		SimpleWorker sw2 = new SimpleWorker();
		Thread th2 = new Thread(sw2);

		th.start();
		th2.start();
	}
	
	public void go() {
		RunnableWorker w1 = new RunnableWorker();
		RunnableWorker w2 = new RunnableWorker();

		Thread th1 = new Thread(w1, "Worker 1");
		Thread th2 = new Thread(w2, "Worker 2");
		
		//Spawn and start the Threads
		th1.start(); 
		th2.start();
		
		//Wait for them to finish
		try {
			th1.join();
			th2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		int finalResult = w1.getResult() + w2.getResult();
		System.out.println("Final Result is " + finalResult);
	}
	
	
	class SimpleWorker implements Runnable
	{
		public void run() {
			for(;;) {
				System.out.println("Hello from " + Thread.currentThread().getName());
			}
		}
	}
	
	/**
	 * Use a Runnable class to create a task
	 * Each Worker will do some calculation and
	 * have a result.  
	 * @author whynot
	 *
	 */
	class RunnableWorker implements Runnable
	{
		private int sum = 0;
		public void run() {
			for(int i = 0; i < 1000; i++) {
				sum += i;
			}
		}
		
		public int getResult() {
			return sum;
		}
	}
}
