package ttl.intjava.threads.racecondition;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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
public class RaceCondition extends Thread {

    private static final int numThreads = 5;

    private final int repeat = 50;

    // The data
    private Repository repository;

    // Each thread stores the counter values it sees in a List
    private List<Integer> numbers = new ArrayList<Integer>();

    // To keep track of the number of Threads
    private static List<RaceCondition> racers = new ArrayList<RaceCondition>();

    public RaceCondition(String name, Repository rep) {
        super(name);
        this.repository = rep;
    }

    public void run() {
        // Spin in a loop and update the counter
        for (int i = 0; i < repeat; i++) {

            // Add the value you see to your own list of numbers
            int val = repository.getData();
            numbers.add(val);
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            int result = ThreadLocalRandom.current().nextInt(1, 10);
            // And add the result into the value
            repository.setData(val + result);
        }
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public static void main(String[] args) {
        Instant start = Instant.now();
        Repository rep = new Repository();
        for (int i = 0; i < numThreads; i++) {
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
            allNums.addAll(rc.numbers);
        }

        Collections.sort(allNums);

        System.out.println("All nums = " + allNums);

        // Look for duplicates. If all has gone well, there should
        // not be any duplicates.
        System.out.println("Duplicates: ");
        Integer last = null;
        int end = allNums.size();

        for (int i = 0; i < end; i++) {
            Integer curr = allNums.get(i);
            if (curr.equals(last)) {
                System.out.print(last + " ");
            }
            last = curr;
        }

        long dur = Duration.between(start, Instant.now()).toMillis();

        System.out.println("Racer took (ms): " + dur);
    }
}
