package ttl.advjava.refplus;

import ttl.advjava.mytry.TryWrap;

/**
 * @author whynot
 */
public class ArrayVisibility {

    private int var;
    private int arr[] = new int[10];


    public class Worker1 extends Thread {
        @Override
        public void run() {
            while(arr[2] != 10) {

            }

            System.out.println("var is " + var);
        }
    }

    public class Worker2 extends Thread {
        @Override
        public void run() {
            var = 100;
            arr[2] = 10;
        }
    }

    public static void main(String[] args) {
        new ArrayVisibility().go();
    }

    public void go() {
        Worker1 w1 = new Worker1();
        Worker2 w2 = new Worker2();

        w1.start();
        w2.start();

        TryWrap<?> tw1 = TryWrap.of(() -> w2.join());
        TryWrap<?> tw2 = TryWrap.of(() -> w2.join());

        System.out.println("All Done");
    }
}
