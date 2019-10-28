package ttl.advjava.threads;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Inspired by an excellent article by Igor Ostrovsky at:
 * http://igoro.com/archive/gallery-of-processor-cache-effects/
 *
 * @author whynot
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 2, time = 2)
@State(Scope.Benchmark)
public class FalseSharingBenchMark {

    private int[] arr = new int[1024];

    @Benchmark
    @Group("falseSharing")
    public void one(Blackhole blackHole) {
        int index = 1;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("falseSharing")
    public void two(Blackhole blackHole) {
        int index = 2;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("falseSharing")
    public void three(Blackhole blackHole) {
        int index = 3;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("falseSharing")
    public void four(Blackhole blackHole) {
        int index = 4;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("noSharing")
    public void sixteen(Blackhole blackHole) {
        int index = 16;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("noSharing")
    public void thirtyTwo(Blackhole blackHole) {
        int index = 32;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("noSharing")
    public void fortyEight(Blackhole blackHole) {
        int index = 48;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    @Benchmark
    @Group("noSharing")
    public void sixtyFour(Blackhole blackHole) {
        int index = 64;
        for (int j = 0; j < 100000000; j++) {
            arr[index] = arr[index] + 3;
            blackHole.consume(arr[index]);
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FalseSharingBenchMark.class.getSimpleName())
                .forks(1)
                //.jvmArgs("-Xms3048m", "-Xmx3048m")
                //.jvmArgs("-XX:+PrintCompilation", "-verbose:gc")
                .build();

        new Runner(opt).run();
    }
}
