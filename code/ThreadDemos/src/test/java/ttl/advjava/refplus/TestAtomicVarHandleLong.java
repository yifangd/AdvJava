//package ttl.advjava.refplus;
//
//import org.junit.jupiter.api.Test;
//
//import java.util.concurrent.CompletableFuture;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
///**
// * @author whynot
// */
//public class TestAtomicVarHandleLong {
//
//    @Test
//    public void testConcurrentIncrement() {
//        UseVarHandleForAtomicLong aul = new UseVarHandleForAtomicLong();
//
//
//        CompletableFuture<?> first = CompletableFuture.runAsync(() -> {
//            for (int i = 0; i < 1000; i++) {
//                aul.incrementAndGet();
//            }
//        });
//
//        CompletableFuture<?> second = CompletableFuture.runAsync(() -> {
//            for (int i = 0; i < 1000; i++) {
//                aul.incrementAndGet();
//            }
//        });
//
//        first.join();
//        second.join();
//
//        assertTrue(aul.getCounter() == 2000);
//    }
//
//    @Test
//    public void testIncrementAndGet() {
//        UseVarHandleForAtomicLong aul = new UseVarHandleForAtomicLong();
//
//        long sum = 0;
//        for (int i = 0; i < 1000; i++) {
//            long x = aul.incrementAndGet();
//            sum += x;
//        }
//
//
//        assertTrue(aul.getCounter() == 1_000);
//        assertTrue(sum == 500_500);
//
//        System.out.println("At end: " + aul.getCounter() +
//                ", sum: " + sum);
//    }
//
//}
