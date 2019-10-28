package ttl.advjava.compfu;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableGames {
	
	public static void main(String[] args) {
		new CompletableGames().builtInPoolHasDaemonThreads();
		System.out.println("All Done");
	}
	
	private ExecutorService es = Executors.newCachedThreadPool();
	
	/**
	 * If you create the CompletableFuture without the 
	 * 'es' argument, then you never see the output from the
	 * future because it will run in the common Thread Pool which
	 * has (*has* to have) daemon threads, so main finishes and the 
	 * VM exits
	 */
	public void builtInPoolHasDaemonThreads() {
		CompletableFuture.supplyAsync(() -> {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Future Returning 10");
			return 10;
		}, es);
	}

}
