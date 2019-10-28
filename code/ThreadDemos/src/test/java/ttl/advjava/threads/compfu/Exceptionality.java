package ttl.advjava.threads.compfu;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class Exceptionality {

    private String evilNullPointer = null;
    private ExecutorService exService = Executors.newFixedThreadPool(15);

    @Test
    public void reallyReallySimple() {
        prmn();
        CompletableFuture<Integer> comp = CompletableFuture.completedFuture(10);
        prwt("Simple Value: " + comp.join());
    }

    /**
     * If you run this as is, sometimes it will not print the output.
     * <p>
     * To show this consistently, add a Thread.sleep(100) before the print.
     * <p>
     * This is the effect of the common ForkJoin Pool having daemon threads.
     * If the main thread dies before the async starts running, the VM dies
     * because there are no more foreground threads
     * <p>
     * To get around this, run in your own Executor Thread Pool.
     * <p>
     * I.e., if 'main' is your only thread, (as opposed to servers), you
     * MUST supply a thread pool or face VM death
     */
    @Test
    public void justACompletable() {
        prmn();
        CompletableFuture<String> comp = CompletableFuture.supplyAsync(() -> {
            //TryWrap.of(() -> TimeUnit.MILLISECONDS.sleep(100));
            prwt("Returning Boo");
            return "Boo";
        });
    }

    /**
     * Without the Thread.sleep, chances are that the supplyAsync thread
     * will finish before the thenAccept call is made - meaning the thenAccept
     * call will run in the calling thread (main thread by default).  This in
     * effect means that the calling thread is blocked.  Async Shmaysync
     * <p>
     * If you don't want the calling thread to block you can run the function
     * in an Executor thread.
     * <p>
     * With the Thread.sleep the thenAccept will probably run in the same thread
     * as the supplyAsync
     */
    @Test
    public void playWithTiming() {
        prmn();
        //@formatter:off
        CompletableFuture.supplyAsync(() -> {
			/*
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			 */
            prwt("Returning Boo");
            return "Boo";
        }, exService)
                .thenAccept(s -> prwt("Accept got " + s));
        //@formatter:on
    }

    @Test
    public void simpleException() {
        prmn();
        //@formatter:off
        CompletableFuture.supplyAsync(() -> {
            prwt("Calling evil guy dot length");
            // return "Should have throw boo";
            return evilNullPointer.length();
        }, exService)
                // The Accept will NOT execute
                .thenAccept((s) -> prwt(s))
                .exceptionally((t) -> {
                    prwt("Exceptionally: " + t.getMessage());
                    t.printStackTrace();
                    return null;
                });
        //@formatter:on
    }

    @Test
    public void secondLevelException() {
        prwt("secondLevelException");
        CompletableFuture.supplyAsync(() -> {
            prwt("Returning Good String");
            return "Good String";
        }).thenApply((s) -> {
            prwt("Apply got " + s);
            return evilNullPointer.length();
            //return 10;
        }).thenAccept(len -> prwt("Last Accept got: " + len))
                .exceptionally((t) -> {
                    prwt("Exceptionally: " + t.getMessage());
                    t.printStackTrace();
                    return null;
                });
    }

    private AtomicInteger counter = new AtomicInteger(0);

    @Test
    public void checkedToUnchecked() {
        prwt("checkedToUnchecked");
        //@formatter:off
        CompletableFuture.supplyAsync(() -> {
            prwt("Returning BadFileName");
            return "BadFileName";
        }, exService).thenApplyAsync((s) -> {
            prwt("Apply got " + s);
            try (FileInputStream fis = new FileInputStream(s)) {
                int i = fis.read();
                prwt("Read " + i);
                return i;
            } catch (IOException e) {
                throw new RuntimeException("Caught IOException", e);
            }
        }, exService)
                .thenAcceptAsync(i -> {
                    prwt("Final Accept result is " + i);
                }, exService)
                .exceptionally((t) -> {
                    counter.incrementAndGet();
                    prwt("Exception: " + t.getMessage());
                    t.printStackTrace();
                    return null;
                });
        //@formatter:on
    }

    @Test
    public void checkedToUncheckedInCallingThread() {
        assertThrows(RuntimeException.class, () -> {
            prwt("checkedToUncheckedInCallingThread");
            CompletableFuture<Integer> comp = CompletableFuture.supplyAsync(() -> {
                prwt("Returning BadFileName");
                return "BadFileName";
            }).thenApply((s) -> {
                prwt("Apply got " + s);
                try (FileInputStream fis = new FileInputStream(s)) {
                    int i = fis.read();
                    prwt("Read " + i);
                    return i;
                } catch (IOException e) {
                    throw new RuntimeException("Caught IOException", e);
                }
            });

            int result = comp.join();
            //The join above will give you a stack trace that will
            //*NOT* include the comp.join() itself.  If you want it
            //to show up, you can catch the Exception from the join and
            //rethrow it, as below
//        try {
//            int result = comp.join();
//        }catch(RuntimeException e) {
//            throw new RuntimeException("From Join", e);
//        }
            prwt("Final Result is " + result);
        });
    }

    @Test
    public void handle() {
        prwt("handle");
        CompletableFuture.supplyAsync(() -> {
            prwt("Returning BadFileName");
            return "BadFileName";
        }, exService).thenApplyAsync((s) -> {
            prwt("Apply got " + s);
            try (FileInputStream fis = new FileInputStream(s)) {
                int i = fis.read();
                prwt("Read " + i);
                return i;
            } catch (IOException e) {
                throw new RuntimeException("Caught IOException", e);
            }
        }, exService).handleAsync((i, e) -> {
            if (i != null) {
                prwt("Handle result is " + i);
                return i;
            } else {
                prwt("Handle Exception: " + e.getMessage());
                e.printStackTrace();
                return -1;
            }
        }, exService).thenAcceptAsync(i -> prwt("Final Result is " + i), exService);
    }

    @Test
    public void whenComplete() {
        prwt("whenComplete");
        CompletableFuture.supplyAsync(() -> {
            prwt("Returning BadFileName");
            return "BadFileName";
        }, exService).thenApplyAsync((s) -> {
            prwt("Apply got " + s);
            try (FileInputStream fis = new FileInputStream(s)) {
                int i = fis.read();
                prwt("Read " + i);
                return i;
            } catch (IOException e) {
                prwt("Caught IOException");
                throw new RuntimeException("Caught IOException", e);
            }
        }, exService)
                .whenCompleteAsync((i, e) -> {
                    if (i != null) {
                        prwt("WhenComplete result is " + i);
                    } else {
                        prwt("WhenComplete Exception: " + e.getMessage());
                        e.printStackTrace();
                    }
                }, exService)
                .thenAcceptAsync(i -> prwt("Final Result is " + i), exService);
    }

    private String tn() {
        return Thread.currentThread().getName();
    }

    private void prwt(Object msg) {
        System.out.println(tn() + ":" + msg);
    }

    private void prmn() {
        prwt(getMethodName());
    }

    public String getMethodName() {
        return new Exception().getStackTrace()[2].getMethodName();
    }


    public static void main(String[] args) {
        Exceptionality ex = new Exceptionality();
        //ex.reallyReallySimple();
        //ex.justACompletable();
        ex.playWithTiming();
        //ex.exService.submit(() -> ex.playWithTiming());
        //ex.simpleException();
        //ex.secondLevelException();
        //ex.checkedToUnchecked();
        //ex.checkedToUncheckedInCallingThread();
        //ex.handle();
        //ex.whenComplete();

//		ex.prwt("Main Going to Exit. Press Enter to End");
//		String line = new Scanner(System.in).nextLine();
//
        ex.exService.shutdown();


    }
}
