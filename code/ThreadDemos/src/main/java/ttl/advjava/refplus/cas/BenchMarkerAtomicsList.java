package ttl.advjava.refplus.cas;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
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

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * @author whynot
 */
@BenchmarkMode({Mode.AverageTime})
//@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
//@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
//@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
public class BenchMarkerAtomicsList {

    private FieldUpdaterForAtomicLong fieldU = new FieldUpdaterForAtomicLong();
    private VarHandleForAtomicLong varHU = new VarHandleForAtomicLong();
    private UnsafeForAtomicLong unsafeU = new UnsafeForAtomicLong();
    private LongAdderForAtomicLong longAdderU = new LongAdderForAtomicLong();
    private SynchronizedForAtomicLong syncU = new SynchronizedForAtomicLong();
    private AtomicLongForAtomicLong atomLongU = new AtomicLongForAtomicLong();

    @State(Scope.Benchmark)
    public static class ListSupplier {
        @Param({"10000000"})
        public int listSize;

        public List<Integer> bunchOfLongs;

        @Setup(Level.Trial)
        public void setUp() {
            System.out.println("Calling setup for State");
            bunchOfLongs = new Random(10)
                    .ints()
                    .limit(listSize)
                    .boxed()
                    .collect(Collectors.toList());
        }
    }

    @Setup(Level.Trial)
    public void setup() {
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        System.out.print("Did shutdown ");
    }

    @Benchmark
    public void testFieldUpdater(ListSupplier ls, Control control, Blackhole sink) {
        ls.bunchOfLongs.stream().parallel().forEach(fieldU::add);
        long sum = fieldU.getCounter();
        sink.consume(sum);
    }

    @Benchmark
    public void testVarHandleUpdate(ListSupplier ls, Control control, Blackhole sink) {
        ls.bunchOfLongs.stream().parallel().forEach(varHU::add);
        long sum = varHU.getCounter();
        sink.consume(sum);
    }

    @Benchmark
    public void testUnsafeUpdate(ListSupplier ls, Control control, Blackhole sink) {
        ls.bunchOfLongs.stream().parallel().forEach(unsafeU::add);
        long sum = unsafeU.getCounter();
        sink.consume(sum);
    }

//    @Benchmark
    public void testLongAdderUpdate(ListSupplier ls, Control control, Blackhole sink) {
        LongAdder la = new LongAdder();
        ls.bunchOfLongs.stream().parallel().forEach(la::add);
        long sum = la.sum();
        sink.consume(sum);
    }

//    @Benchmark
    public void testAtomicLongPlain(ListSupplier ls, Control control, Blackhole sink) {
        AtomicLong al = new AtomicLong(0);
        ls.bunchOfLongs.stream().parallel().forEach(al::addAndGet);
        long sum = al.get();
        sink.consume(sum);
    }

//    @Benchmark
    public void testAtomicLongAtomicLong(ListSupplier ls, Control control, Blackhole sink) {
        ls.bunchOfLongs.stream().parallel().forEach(atomLongU::add);
        long sum = atomLongU.getCounter();
        sink.consume(sum);
    }

//    @Benchmark
    public void testSynchronizedAtomicLong(ListSupplier ls, Control control, Blackhole sink) {
        ls.bunchOfLongs.stream().parallel().forEach(syncU::add);
        long sum = syncU.getCounter();
        sink.consume(sum);
    }

//    @Benchmark
    public void testWithJavaStream(ListSupplier ls, Control control, Blackhole sink) {
        long sum = ls.bunchOfLongs.stream().parallel()
                .mapToLong(Integer::longValue)
                .sum();
        sink.consume(sum);
    }



    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BenchMarkerAtomicsList.class.getSimpleName())
                .forks(1)
                //.addProfiler("perfasm")
                .jvmArgs("-XX:+UnlockDiagnosticVMOptions","-XX:+PrintAssembly")
                //.jvmArgs("-Xms3048m", "-Xmx3048m")
                //.jvmArgs("-XX:+PrintGCDetails")  //jdk8
                //.jvmArgs("-Xlog:gc*")              //jdk11
                .build();

        new Runner(opt).run();
    }
}
