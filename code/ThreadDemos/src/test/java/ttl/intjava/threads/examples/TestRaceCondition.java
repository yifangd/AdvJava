package ttl.intjava.threads.examples;

import org.junit.jupiter.api.Test;
import ttl.intjava.threads.racecondition.RaceCondition;
import ttl.intjava.threads.racecondition.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Program to illustrate a race condition. We create a bunch of threads which
 * are accessing a shared Repository of data. The threads spin in a loop and
 * increment a counter in the repository. We want the counter to always have a
 * coherent value, which is one more than it's last value. So, if we have 5
 * threads that spin around 50 times each, we want the final value of the
 * counter to be 249.
 * <p/>
 * Also demonstrate the use of java.util.concurrent.CountDownLatch.
 *
 * @author Anil Pal Date: Apr 28, 2010 Time: 10:46:34 PM
 */
public class TestRaceCondition {

    private static final int numRacers = 5;

    // To keep track of the number of Threads
    private static List<RaceCondition> racers = new ArrayList<>();



    @Test
    public void testThatNoDuplicatesAreProduced() {
        Instant start = Instant.now();
        Repository rep = new Repository();
        for (int i = 0; i < numRacers; i++) {
            RaceCondition rc = new RaceCondition(i + 1 + "", rep);
            racers.add(rc);
            rc.start();
        }


        // Collect all the numbers into one list
        List<Integer> allNums = new ArrayList<Integer>();
        for (RaceCondition rc : racers) {
            try {
                rc.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // System.out.println(rc.numbers);
            allNums.addAll(rc.getNumbers());
        }

        Collections.sort(allNums);

//        System.out.println("All nums = " + allNums);

        // Look for duplicates. If all has gone well, there should
        // not be any duplicates.
        System.out.println("Duplicates: ");
        List<Integer> dups = new ArrayList<>();
        Integer last = null;
        int end = allNums.size();

        for (int i = 0; i < end; i++) {
            Integer curr = allNums.get(i);
            if (curr.equals(last)) {
                dups.add(curr);
                System.out.print(last + " ");
            }
            last = curr;
        }

        long dur = Duration.between(start, Instant.now()).toMillis();

        System.out.println("Racer took (ms): " + dur);
        assertEquals(0, dups.size());
    }
}
