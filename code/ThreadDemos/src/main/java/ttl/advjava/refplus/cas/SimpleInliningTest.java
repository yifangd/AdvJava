package ttl.advjava.refplus.cas;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author whynot
 */
// The Sandbox is designed to help you learn about the HotSpot JIT compilers.
// Please note that the JIT compilers may behave differently when isolating a method
// in the Sandbox compared to running your whole application.

public class SimpleInliningTest
{
    public SimpleInliningTest()
    {
        int sum = 0;
        int sum2 = 0;
        int sum3 = 0;

        int [] arr = new int[1024];

        // 1_000_000 is F4240 in hex
        for (int i = 0 ; i < 1_000_000; i++)
        {
            sum = this.add(sum, 99); // 63 hex
            sum2 += this.sameLoc(arr, ThreadLocalRandom.current().nextInt(1_000_000, 1_000_010));
            sum3 += this.differentLoc(arr, ThreadLocalRandom.current().nextInt(1_000_000, 1_000_010));
        }

        System.out.println("Sum:" + sum + ", sum2: " + sum2);
    }

    public int add(int a, int b)
    {
        return a + b;
    }

    public int sameLoc(int [] arr, int steps) {
        Instant start = Instant.now();
        for(int i = 0; i < steps; i++) {
            arr[0]++;
            arr[0]++;
        }
//        System.out.println("same loc took: " + start.until(Instant.now(), ChronoUnit.MILLIS));
        return arr[0];
    }

    public int differentLoc(int [] arr, int steps) {
        Instant start = Instant.now();
        for(int i = 0; i < steps; i++) {
            arr[0]++;
            arr[1]++;
        }
//        System.out.println("different loc took: " + start.until(Instant.now(), ChronoUnit.MILLIS));
        return arr[1];
    }

    public static void main(String[] args)
    {
        new SimpleInliningTest();
    }
}
