package ttl.advjava.queues;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.GroupThreads;
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
import ttl.advjava.refplus.cas.BenchMarkerAtomics;
import ttl.advjava.refplus.cas.BenchMarkerAtomicsList;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whynot
 */
@BenchmarkMode({Mode.Throughput})
//@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@State(Scope.Group)
public class BenchMarker {

    @State(Scope.Group)
    public static class MyState {
        public int reps = 1_000_000;
        public ManyQueues manyQueues;
        public ManyQueues.ProducerQueueAtomic producerAtomic;
        public ManyQueues.ConsumerQueueAtomic consumerAtomic;

        public ManyQueues.ProducerQueueAtomicLazy producerAtomicLazy;
        public ManyQueues.ConsumerQueueAtomicLazy consumerAtomicLazy;

        public ManyQueues.ProducerQueueAtomicLazyMask producerAtomicLazyMask;
        public ManyQueues.ConsumerQueueAtomicLazyMask consumerAtomicLazyMask;

        public AtomicInteger counter;

        public MyState() {
            init();
        }
        public void init() {
            counter = new AtomicInteger(0);
            manyQueues = new ManyQueues(10);
            producerAtomic = manyQueues.new ProducerQueueAtomic(reps);
            consumerAtomic = manyQueues.new ConsumerQueueAtomic(reps);

            producerAtomicLazy = manyQueues.new ProducerQueueAtomicLazy(reps);
            consumerAtomicLazy = manyQueues.new ConsumerQueueAtomicLazy(reps);

            producerAtomicLazyMask = manyQueues.new ProducerQueueAtomicLazyMask(reps);
            consumerAtomicLazyMask = manyQueues.new ConsumerQueueAtomicLazyMask(reps);
            System.out.print("Did State setup ");
        }

        @TearDown(Level.Iteration)
        public void clear() {
            System.out.println("On teardown, finalResult is " +
                    manyQueues.getFinalResult() +
                    ", Atomic: " + producerAtomic.prodCount +
                    ", Lazy: " + producerAtomicLazy.prodCount +
                    ", Lazy Mask: " + producerAtomicLazyMask.prodCount);
            init();
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
    @Group("prodcon")
    @GroupThreads(1)
    public void producer(MyState ms, Control control, Blackhole sink) {
        //System.out.println("Producer called");
        if(!control.stopMeasurement) {
            sink.consume(ms.producerAtomic.put());
        }
    }

    @Benchmark
    @Group("prodcon")
    @GroupThreads(1)
    public void consumer(MyState ms, Control control, Blackhole sink) {
        if(!control.stopMeasurement) {
            //System.out.println("Consumer called");
            sink.consume(ms.consumerAtomic.get());
        }
    }

    @Benchmark
    @Group("prodconlazy")
    @GroupThreads(1)
    public void producerLazy(MyState ms, Control control, Blackhole sink) {
        //System.out.println("Producer called");
        if(!control.stopMeasurement) {
            sink.consume(ms.producerAtomicLazy.put());
        }
    }

    @Benchmark
    @Group("prodconlazy")
    @GroupThreads(1)
    public void consumerLazy(MyState ms, Control control, Blackhole sink) {
        if(!control.stopMeasurement) {
            //System.out.println("Consumer called");
            sink.consume(ms.consumerAtomicLazy.get());
        }
    }

    @Benchmark
    @Group("prodconlazymask")
    @GroupThreads(1)
    public void producerLazyMask(MyState ms, Control control, Blackhole sink) {
        //System.out.println("Producer called");
        if(!control.stopMeasurement) {
            sink.consume(ms.producerAtomicLazyMask.put());
        }
    }

    @Benchmark
    @Group("prodconlazymask")
    @GroupThreads(1)
    public void consumerLazyMask(MyState ms, Control control, Blackhole sink) {
        if(!control.stopMeasurement) {
            //System.out.println("Consumer called");
            sink.consume(ms.consumerAtomicLazyMask.get());
        }
    }

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BenchMarker.class.getSimpleName())
                .exclude(BenchMarkerAtomics.class.getSimpleName())
                .exclude(BenchMarkerAtomicsList.class.getSimpleName())
                .forks(1)
                //.jvmArgs("-Xms3048m", "-Xmx3048m")
                //.jvmArgs("-XX:+PrintCompilation", "-verbose:gc")
                .build();

        new Runner(opt).run();
    }
}
