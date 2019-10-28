package ttl.advjava.refplus.cas;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

/**
 * Use VarHandles to create a simple Atomic Long.  Prefer VarHandles
 * to unsafe from JDK 9 onwards
 * @author whynot
 *
 */

public class VarHandleForAtomicLong {
    private volatile long counter = 0;

    private final static VarHandle TARGET;

    /**
     * Standard incantation to get a reference to a VarHandle.
     * Note that, unlike an AtomicInteger which requires a reference
     * to an Object, the VarHandle is static;
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    static {
        try {
            TARGET = MethodHandles.lookup()
                    .findVarHandle(VarHandleForAtomicLong.class,
                            "counter", long.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public VarHandleForAtomicLong() {
    }

    public long incrementAndGet() {
        long before;
        //Here's the "Atomic" part.  The hope is that the Cas will
        //ultimately succeed.
//        do {
//            before = counter;
//            next = before + 1;
//        } while (!TARGET.compareAndSet(this, before, next));

        for(;;) {
            before = counter;
            if(TARGET.compareAndSet(this, before, before + 1)) {
                return before + 1;
            }
            LockSupport.parkNanos(1);
        }
    }

    public long add(long value) {
        long before;
        long next;
        //Here's the "Atomic" part.  The hope is that the Cas will
        //ultimately succeed.

//        do {
//            before = counter;
//            next = before + value;
//        } while (!TARGET.compareAndSet(this, before, next));
        for(;;) {
            before = counter;
            if(TARGET.compareAndSet(this, before, before + value)) {
                return before + value;
            }
            LockSupport.parkNanos(1);
        }
    }


    public long getCounter() {
        return counter;
    }


    public static void main(String[] args) throws Exception {
	}
}
