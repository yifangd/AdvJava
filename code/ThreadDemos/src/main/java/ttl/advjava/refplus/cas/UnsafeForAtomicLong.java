package ttl.advjava.refplus.cas;


import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Use Unsafe to create a simple Atomic Long.  Prefer VarHandles
 * to unsafe from JDK 9 onwards
 *
 * @author whynot
 */
public class UnsafeForAtomicLong {
    private static final Unsafe unsafe;
    private static final long offset;
    private volatile long counter = 0;

    static {
        try {
            unsafe = getUnsafe();
            //get and save the offset of our volatile field
            offset = unsafe.objectFieldOffset(UnsafeForAtomicLong.class.getDeclaredField("counter"));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    /**
     * Standard incantation to get a reference to an Unsafe object
     *
     * @return
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     */
    private static Unsafe getUnsafe() throws IllegalAccessException, NoSuchFieldException {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    public UnsafeForAtomicLong() {
    }

    public long incrementAndGet() {
        //Call getAndAddLong directory
        long before = unsafe.getAndAddLong(this, offset, 1L);
        return before + 1;
    }

    public long getAndIncrement() {
        //Call getAndAddLong directory
        long before = unsafe.getAndAddLong(this, offset, 1L);
        return before;
    }


    public void add(long value) {
        unsafe.getAndAddLong(this, offset, counter + value);
    }

    public long getCounter() {
        return counter;
    }


    public static void main(String[] args) throws Exception {
    }
}
