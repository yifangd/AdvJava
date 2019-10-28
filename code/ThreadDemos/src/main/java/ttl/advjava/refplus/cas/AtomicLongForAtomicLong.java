package ttl.advjava.refplus.cas;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public class AtomicLongForAtomicLong {

    private final static AtomicLong counter = new AtomicLong();

    public AtomicLongForAtomicLong() {
    }
 
    public long incrementAndGet() {
        long before;
        //Here's the "Atomic" part.  The hope is that the Cas will
        //ultimately succeed.
        do {
            before = counter.get();
        }while (!counter.compareAndSet(before, before + 1));
        return before + 1;
    }

    public void add(long value) {
        long before;
        //Here's the "Atomic" part.  The hope is that the Cas will
        //ultimately succeed.
        for(;;) {
            before = counter.get();
            if (counter.compareAndSet(before, before + value)) {
                return;
            } else {
                LockSupport.parkNanos(1);
            }
        }
    }

    public long getCounter() {
        return counter.longValue();
    }
    
    
    public static void main(String[] args) throws Exception {
	}
}
