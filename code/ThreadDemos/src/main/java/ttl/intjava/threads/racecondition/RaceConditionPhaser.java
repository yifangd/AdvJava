package ttl.intjava.threads.racecondition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Program to illustrate a race condition. We create a bunch of threads which
 * are accessing a shared Repository of data. The threads spin in a loop and
 * increment a counter in the repository. We want the counter to always have a
 * coherent value, which is one more than it's last value. So, if we have 5
 * threads that spin around 50 times each, we want the final value of the
 * counter to be 249.
 * <p/>
 * Also demonstrate the use of java.util.concurrent.Phaser.
 *
 * @author Anil Pal Date: Apr 28, 2010 Time: 10:46:34 PM
 */
public class RaceConditionPhaser extends Thread {

    private static final int numThreads = 5;
    // This holds each thread at the "start gate", waiting for it to
    // be "opened".
    private static Phaser phaser;

    private final int repeat = 50;

    // The data
    private Repository repository;

    // Each thread stores the counter values it sees in a List
    private List<Integer> numbers = new ArrayList<Integer>();

    // To keep track of the number of Threads
    private static List<RaceConditionPhaser> racers = new ArrayList<RaceConditionPhaser>();

    public RaceConditionPhaser(String name, Repository rep) {
        super(name);
        this.repository = rep;
        phaser.register();
    }

    public void run() {

        //Wait for everyone else to get to the phaser before we proceed
        phaser.arriveAndAwaitAdvance();

        // Spin in a loop and update the counter
        for (int i = 0; i < repeat; i++) {

            synchronized (repository) {
                // Add the value you see to your own list of numbers
                int val = repository.getData();
                numbers.add(val);
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                // And increment the value
                // And increment the value
                int result = ThreadLocalRandom.current().nextInt(1, 10);
                // And add the result into the value
                repository.setData(val + result);
            }
        }

        // Now arrive and deregister from the phaser
        // When the last one has arrived and deregistered,
        //the results can get processed in onAdvance
        phaser.arriveAndDeregister();
    }

    public static void main(String[] args) {
        Repository rep = new Repository();
        phaser = new Phaser() {
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("Phaser.onAdvance, phase = " + phase
                        + ", registered = " + registeredParties);
                //This will be true after all parties
                //Have finished their jobs and deregistered
//				if (registeredParties == 1) {
//					//bye bye
//					return true;
//				}

                //An alternate way to end the phaser.  See the note
                //in 'main' below.
                if (registeredParties == 0) {
                    processResults();
                    return true;
                }

                return false;
            }
        };

        //Register one object with phaser before starting, so
        //everyone has to wait till you arriveAndDeregister (below)
        phaser.register();

        for (int i = 0; i < numThreads; i++) {
            RaceConditionPhaser rc = new RaceConditionPhaser(i + 1 + "", rep);
            racers.add(rc);
            rc.start();
        }

        //If you want to die here, you can send the parties
        //the to the advance and process results in
        //onAdvance when registeredParties falls to 0
        phaser.arriveAndDeregister();


        //Or you can not deregister, and have the phaser
        //quit in onAdvance when the registeredParties hits 1
//		while(!phaser.isTerminated()) {
//			phaser.arriveAndAwaitAdvance();
//		}

        processResults();
        System.out.println("\nwe are good too");
    }

    public static void processResults() {

        // Collect all the numbers into one list
        List<Integer> allNums = new ArrayList<Integer>();
        for (RaceConditionPhaser rc : racers) {
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
    }
}
