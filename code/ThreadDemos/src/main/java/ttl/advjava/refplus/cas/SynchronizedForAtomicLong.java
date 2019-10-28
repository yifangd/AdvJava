package ttl.advjava.refplus.cas;

/**
 * Use VarHandles to create a simple Atomic Long.  Prefer VarHandles
 * to unsafe from JDK 9 onwards
 * @author whynot
 *
 */

public class SynchronizedForAtomicLong {
    private long counter = 0;

    public SynchronizedForAtomicLong() {
    }

    public synchronized long incrementAndGet() {
        return counter++;
    }

    public synchronized long add(long value) {
        return counter += value;
    }


    public synchronized long getCounter() {
        return counter;
    }


    public static void main(String[] args) throws Exception {
	}
}
