package ttl.intjava.threads.customexec;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An Executor which uses the afterExecute hook to catch Exceptions in Tasks
 * @author whynot
 *
 */
public class ExceptionHandlingCustomExecutor extends ThreadPoolExecutor {

	private AtomicInteger counter = new AtomicInteger(0);

	public ExceptionHandlingCustomExecutor(int fixedPoolSize) {
		this(fixedPoolSize, fixedPoolSize, 0, TimeUnit.MILLISECONDS, 
				new LinkedBlockingQueue<Runnable>() );
	}

	public ExceptionHandlingCustomExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                           BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);

		//You can use this to customize the Threads in your ThreadPool
		//Note that this Runnable r is *NOT* the same as the one you will
		//supply for your tasks.  This is the one that the ExecutorService
		//uses to run your tasks.  Each Thread in your thread pool has an
		//associated Runnable
		ThreadFactory tf = (r) -> {
			//Give your threads a name that is identifiable.  
			Thread th = new Thread(r, "CustomExec-" + counter.getAndIncrement());
			return th;
		};
		
		this.setThreadFactory(tf);
	}

	/**
	 * 
	 */
	@Override
	protected void beforeExecute(Thread th, Runnable task) {
		super.beforeExecute(th,  task);
	}
	
	/**
	 * Check for any Exceptions thrown.  If the task was a Runnable or
	 * Callable, then the Runnable will actually be a Future and that
	 * future will hold any Exception, so you need to reach into it
	 * and call get, which will throw the Exception
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            System.out.println(t);
        }	
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		ExceptionHandlingCustomExecutor exec = new ExceptionHandlingCustomExecutor(8);

		Callable<Double> c2 = () -> 22.5;
		Future<Double> f = exec.submit(c2);
		System.out.println("f is " + f.get());
		
		Callable<Double> c1 = () -> { throw new RuntimeException("Callable Ouch"); };
		
		exec.submit(() -> {
			throw new RuntimeException("Ouch");
		});

		exec.submit(c1);

	}
}
