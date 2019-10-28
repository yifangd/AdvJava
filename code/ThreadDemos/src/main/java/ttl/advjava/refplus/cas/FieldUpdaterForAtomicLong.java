package ttl.advjava.refplus.cas;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Use VarHandles to create a simple Atomic Long.  Prefer VarHandles
 * to unsafe from JDK 9 onwards
 * @author whynot
 *
 */
public class FieldUpdaterForAtomicLong {
    private volatile long counter = 0;

    /**
     * Standard incantation to get a reference to an AtomicLongFieldUpdater
     * Note that, unlike an AtomicInteger which requires a reference
     * to an Object, the AtomicLongFieldUpdater is static;
     */
    private static final AtomicLongFieldUpdater<FieldUpdaterForAtomicLong> counterU;
    static {
        counterU = AtomicLongFieldUpdater.newUpdater(FieldUpdaterForAtomicLong.class,
                "counter");
    }

    public FieldUpdaterForAtomicLong() {
    }
 
    public long incrementAndGet() {
        return counterU.incrementAndGet(this);
    }

    public void add(long l) {
        counterU.getAndAdd(this, l);
    }


    public long getCounter() {
        return counter;
    }
    
    
    public static void main(String[] args) throws Exception {
	}
}
