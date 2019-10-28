package ttl.intjava.threads.memlimit;

import java.util.ArrayList;
import java.util.List;

public class MaxThreads {

    public static void main(String[] args) {
        final int max = 1_000_000;
        List<Thread> threads = new ArrayList<>();
        Worker w = new Worker();
        for(int i = 0; i < max; i++) {
            Thread th = new Thread(w, "Thread" + i);
            th.start();
            threads.add(th);
            System.out.println("Created Thread " + i);
        }
    }

    public static class Worker implements Runnable
    {

        @Override
        public void run() {
           synchronized(this) {
               try {
                   this.wait();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        }
    }

}
