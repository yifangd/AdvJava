package ttl.advjava.refplus.cas;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author whynot
 */
@BenchmarkMode({Mode.Throughput})
//@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchMarkerAtomics {

    private FieldUpdaterForAtomicLong fieldU = new FieldUpdaterForAtomicLong();
    private VarHandleForAtomicLong varHU = new VarHandleForAtomicLong();
    private UnsafeForAtomicLong unsafeU = new UnsafeForAtomicLong();
    private LongAdderForAtomicLong longAdder = new LongAdderForAtomicLong();

    @Setup(Level.Trial)
    public void setup() {
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.print("Did shutdown ");
    }

    @Benchmark
    public void testFieldUpdater(Control control, Blackhole sink) {
        sink.consume(fieldU.incrementAndGet());
    }

    @Benchmark
    public void testVarHandleUpdate(Control control, Blackhole sink) {
        sink.consume(varHU.incrementAndGet());

    }

    @Benchmark
    public void testUnsafeUpdate(Control control, Blackhole sink) {
        sink.consume(unsafeU.incrementAndGet());
    }

//    @Benchmark
//    public void testLongAdderUpdate(Control control, Blackhole sink) {
//        longAdder.increment();
//        sink.l1();
//    }



    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .exclude(BenchMarkerAtomicsList.class.getSimpleName())
                .include(BenchMarkerAtomics.class.getSimpleName())
                //.exclude(BenchMarker.class.getSimpleName())
                .forks(1)
                //.jvmArgs("-Xms3048m", "-Xmx3048m")
                //.jvmArgs("-XX:+PrintGCDetails")  //jdk8
                //.jvmArgs("-Xlog:gc*")              //jdk11
                .build();

        new Runner(opt).run();
    }
}
