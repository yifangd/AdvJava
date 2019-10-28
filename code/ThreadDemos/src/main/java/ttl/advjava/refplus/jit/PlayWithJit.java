package ttl.advjava.refplus.jit;

import ttl.advjava.mytry.TryWrap;

/**
 * -XX:+UnlockDiagnosticVMOptions
 * -XX:+TraceClassLoading
 * -XX:+LogCompilation
 * -XX:+PrintAssembly    //Only if you want assembly
 *
 * Overview by the author at:
 * https://www.infoq.com/presentations/jitwatch/
 *
 * @author whynot
 */
public class PlayWithJit {


    int sum = 0;
    double d = 0;
    int [] arr = new int[10];
    public PlayWithJit() {
    }

    public void go() {
        int times = 2_000;
        // 1_000_000 is F4240 in hex
        for (int i = 0; i < times; i++) {
            sum = this.add(sum, 99); // 63 hex
            d += doBigCalculation(sum);
        }
    }

    public int add(int a, int b) {
        return a + b;
    }

    public double doBigCalculation(int x) {
        double tmp = Math.sin(x);
        tmp *= 1.3334;
        return tmp;
    }

    public static void main(String[] args) {
        PlayWithJit pj1 = new PlayWithJit();
        Thread th = new Thread(() -> {
            pj1.go();
        });

        pj1.go();

        TryWrap.of(() -> th.join());
        System.out.println("sum: " + pj1.sum + ", d: " + pj1.d);
    }
}
