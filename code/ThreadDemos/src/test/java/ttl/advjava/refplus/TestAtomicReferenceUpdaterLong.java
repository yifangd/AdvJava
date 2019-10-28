package ttl.advjava.refplus;

import org.junit.jupiter.api.Test;
import ttl.advjava.refplus.cas.FieldUpdaterForAtomicLong;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author whynot
 */
public class TestAtomicReferenceUpdaterLong {

    @Test
    public void testConcurrentIncrement() {
        FieldUpdaterForAtomicLong aul = new FieldUpdaterForAtomicLong();


        CompletableFuture<?> first = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 1000; i++) {
                aul.incrementAndGet();
            }
        });

        CompletableFuture<?> second = CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 1000; i++) {
                aul.incrementAndGet();
            }
        });

        first.join();
        second.join();

        assertTrue(aul.getCounter() == 2000);
    }

    @Test
    public void testIncrementAndGet() {
        FieldUpdaterForAtomicLong aul = new FieldUpdaterForAtomicLong();

        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            long x = aul.incrementAndGet();
            sum += x;
        }


        assertTrue(aul.getCounter() == 1_000);
        assertTrue(sum == 500_500);

        System.out.println("At end: " + aul.getCounter() +
                ", sum: " + sum);
    }

}
