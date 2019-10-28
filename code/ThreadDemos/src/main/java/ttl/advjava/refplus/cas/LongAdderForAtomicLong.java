package ttl.advjava.refplus.cas;

import java.util.concurrent.atomic.LongAdder;

/**
 * Use VarHandles to create a simple Atomic Long.  Prefer VarHandles
 * to unsafe from JDK 9 onwards
 * @author whynot
 *
 */
public class LongAdderForAtomicLong {

    /**
     * Standard incantation to get a reference to a LongAdder.
     * This class should be faster to use as a counter only.
     * But getting the value is *very* expensive, so it has
     * no getAndXXX or XXXAndGet type methods.
     * It is good for using just as a counter, and then retrieving
     * a value at the end.
     *
     */
    private LongAdder counter = new LongAdder();

    public LongAdderForAtomicLong() {
    }
 
    public void increment() {
        counter.increment();
    }

    public void add(long l) {
        counter.add(l);
    }

    public long getCounter() {
        return counter.longValue();
    }
    
    
    public static void main(String[] args) throws Exception {
	}
}
