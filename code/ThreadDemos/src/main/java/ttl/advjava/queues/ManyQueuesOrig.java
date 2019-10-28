package ttl.advjava.queues;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

/**
 * Inspired by Nitsan Wakart's blog:
 * https://psy-lob-saw.blogspot.com/2013/03/single-producerconsumer-lock-free-queue.html
 * 
 * which itself is based on work by Martin Thompson:
 * https://mechanical-sympathy.blogspot.com/
 * @author whynot
 *
 */
public class ManyQueuesOrig {

	private int transferValue;
	private long finalResult;
	private volatile boolean volatileGuard;
	private boolean plainGuard;

	private AtomicBoolean atomicGuard = new AtomicBoolean();
	private static ExecutorService executor = Executors.newFixedThreadPool(3);

	private AtomicInteger atomicHead = new AtomicInteger(0);
	private AtomicInteger atomicTail = new AtomicInteger(0);
	
	private LinkedBlockingQueue<Integer> lbq;
	private ConcurrentLinkedQueue<Integer> clq;

	private volatile int head = 0;
	private volatile int tail = 0;
	
	private int bufSize = 10;
	private int mask;
	private int[] buffer;

	public static void main(String[] args) {

		executor = Executors.newFixedThreadPool(3);

		ManyQueuesOrig sv = new ManyQueuesOrig(10);
		sv.run();
		//sv.playwithPow();
		
		executor.shutdown();
	}
	
	public void playwithPow() {
		initBuf(10);
		int head = Integer.MAX_VALUE;
		int tail = Integer.MAX_VALUE;

		int index = Math.floorMod(head,  bufSize);
		System.out.printf("index of %,d is %,d:%n", head, index);
		int tailIndex = tail & mask;
		System.out.printf("tail index of %,d is %,d:%n", tail, tailIndex);
		
		head = head + 1;
		index = Math.floorMod(head,  bufSize);
		System.out.printf("index of %,d is %,d:%n", head, index);
		tail = tail + 1;
		tailIndex = tail & mask;
		System.out.printf("tail index of %,d is %,d:%n", tail, tailIndex);

		head = head + 1;
		index = Math.floorMod(head,  bufSize);
		System.out.printf("index of %,d is %,d:%n", head, index);
		tail = tail + 1;
		tailIndex = tail & mask;
		System.out.printf("tail index of %,d is %,d:%n", tail, tailIndex);

		head = head + 1;
		index = Math.floorMod(head,  bufSize);
		System.out.printf("index of %,d is %,d:%n", head, index);
		tail = tail + 1;
		tailIndex = tail & mask;
		System.out.printf("tail index of %,d is %,d:%n", tail, tailIndex);
	}

	public ManyQueuesOrig(int size) {
		initBuf(size);
	}

	public static int findNextPowerOfTwo(int value) {
		return 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
	}

	private void initBuf(int size) {
		bufSize = findNextPowerOfTwo(size);
		buffer = new int[bufSize];
		mask = bufSize - 1;
		finalResult = head = tail = 0;
		atomicHead = new AtomicInteger(0);
		atomicTail = new AtomicInteger(0);
		
		lbq = new LinkedBlockingQueue<Integer>(bufSize);
		clq = new ConcurrentLinkedQueue<Integer>();
	}

	public void run() {

		double val = 0;
		// int reps = 1_000_000;
		int reps = 1_000_000;
		List<Result> queueResults = new ArrayList<>();
		List<Result> atomicResults = new ArrayList<>();
		List<Result> atomicLazyResults = new ArrayList<>();
		List<Result> atomicLazyMaskResults = new ArrayList<>();
		List<Result> atomicLazyMaskYieldResults = new ArrayList<>();
		List<Result> linkedBlockingResults = new ArrayList<>();
		List<Result> concurrentLinkedResults = new ArrayList<>();
		BinaryOperator<Result> mushResults = (prodR, consR) -> {
			Result r = new Result(prodR.name, prodR.reps, prodR.prodTime, consR.consTime, consR.numTrue,
					consR.head, consR.tail, consR.finalResult, consR.seen);
			return r;
		};
		for (int i = 0; i < 5; i++) {

			initBuf(10);
			queueResults.add(
					goCommon(new ProducerQueueVolatile(reps), new ConsumerQueueVolatile(reps), mushResults));

			initBuf(10);
			linkedBlockingResults.add(
					goCommon(new ProducerQueueLinkedBlocking(reps), new ConsumerQueueLinkedBlocking(reps), mushResults));

			initBuf(10);
			concurrentLinkedResults.add(
					goCommon(new ProducerQueueConcurrentLinked(reps), new ConsumerQueueConcurrentLinked(reps), mushResults));

			initBuf(10);
			atomicResults
					.add(goCommon(new ProducerQueueAtomic(reps), new ConsumerQueueAtomic(reps), mushResults));
			initBuf(10);
			atomicLazyResults.add(
					goCommon(new ProducerQueueAtomicLazy(reps), new ConsumerQueueAtomicLazy(reps), mushResults));
			initBuf(10);
			atomicLazyMaskYieldResults.add(goCommon(new ProducerQueueAtomicLazyMaskYield(reps),
					new ConsumerQueueAtomicLazyMaskYield(reps), mushResults));
			initBuf(10);
			atomicLazyMaskResults.add(goCommon(new ProducerQueueAtomicLazyMask(reps),
					new ConsumerQueueAtomicLazyMask(reps), mushResults));
		}

		// System.out.println(plainResults);
		// System.out.println();
		// System.out.println(volatileResults);

		Supplier<Stats> sup = Stats::new;
		BiConsumer<Stats, Result> accum = Stats::accumulate;
		BinaryOperator<Stats> comb = Stats::combine;
		Function<Stats, Stats> fin = Stats::finish;

		Collector<Result, Stats, Stats> collector = Collector.of(sup, accum, comb, fin);

		Stats queueC = queueResults.stream().collect(collector);
		Stats linkedBlockingC = linkedBlockingResults.stream().collect(collector);
		Stats concurrentLinkedC = concurrentLinkedResults.stream().collect(collector);
		Stats atomicC = atomicResults.stream().collect(collector);
		Stats lazyC = atomicLazyResults.stream().collect(collector);
		Stats lazyMaskC = atomicLazyMaskResults.stream().collect(collector);
		Stats lazyMaskYieldC = atomicLazyMaskYieldResults.stream().collect(collector);

		System.out.println("QueueWhile: " + queueC);
		System.out.printf("Volatile: head:%,d, tail:%,d, finalResult:%,d%n", queueC.head, queueC.tail, queueC.finalResult);
		System.out.println();

		System.out.println("LinkedBlocking: " + linkedBlockingC);
		System.out.printf("LinkedBlocking: head:%,d, tail:%,d, finalResult:%,d%n", linkedBlockingC.head, linkedBlockingC.tail, linkedBlockingC.finalResult);
		System.out.println();

		System.out.println("ConcurrentLinked: " + concurrentLinkedC);
		System.out.printf("ConcurrentLinked: head:%,d, tail:%,d, finalResult:%,d%n", concurrentLinkedC.head, concurrentLinkedC.tail, concurrentLinkedC.finalResult);
		System.out.println();


		System.out.println("QueueAtomic: " + atomicC);
		System.out.printf("Atomic: head:%,d, tail:%,d, finalResult:%,d%n", atomicC.head, atomicC.tail, atomicC.finalResult);
		System.out.println();

		System.out.println("QueueLazy: " + lazyC);
		System.out.printf("Lazy: head:%,d, tail:%,d, finalResult:%,d%n", lazyC.head, lazyC.tail, lazyC.finalResult);
		System.out.println();

		System.out.println("QueueLazyMask: " + lazyMaskC);
		System.out.printf("LazyMask: head:%,d, tail:%,d, finalResult:%,d%n", lazyMaskC.head, lazyMaskC.tail, lazyMaskC.finalResult);
		System.out.println();

		System.out.println("QueueLazyMaskYield: " + lazyMaskYieldC);
		System.out.printf("LazyMaskYield: head:%,d, tail:%,d, finalResult:%,d%n", lazyMaskYieldC.head, lazyMaskYieldC.tail, lazyMaskYieldC.finalResult);

	}

	public void startRun() {

	}

	public Result goCommon(Callable<Result> producer, Callable<Result> consumer,
			BiFunction<Result, Result, Result> resultConsumer) {
		Future<Result> producerT = executor.submit(producer);
		Future<Result> consumerT = executor.submit(consumer);

		Result producerR = null;
		Result consumerR = null;
		try {
			producerR = producerT.get();
			consumerR = consumerT.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		Result r = resultConsumer.apply(producerR, consumerR);
		return r;
	}

	public class ProducerQueueVolatile implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueVolatile(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currTail = tail;
				int startIndex = currTail - bufSize;
				while (head <= startIndex) {
					Thread.yield();
				}
				//buffer[Math.floorMod(currTail, bufSize)] = i;
				buffer[currTail % bufSize] = i;
				tail = currTail + 1;
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueVolatile implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueVolatile(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currHead = head;
				while (currHead >= tail) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				//int index = Math.floorMod(currHead, bufSize);
				int index = currHead % bufSize;
				int x = buffer[index];
				buffer[index] = -1;
				finalResult += x;
				numTrue++;
				head = Math.max(0, currHead + 1);
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			//Result r = new Result("", reps, -1, time, numTrue, head, tail, finalResult, seen);
			Result r = new Result("", reps, -1, time, numTrue, head, tail, finalResult);
			return r;
		}
	}


	public class ProducerQueueAtomic implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueAtomic(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currTail = atomicTail.get();
				int startIndex = currTail - bufSize;
				while (atomicHead.get() <= startIndex) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				//buffer[Math.floorMod(currTail, bufSize)] = i;
				buffer[currTail % bufSize] = i;
				atomicTail.set(currTail + 1);
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueAtomic implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueAtomic(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				while (atomicHead.get() >= atomicTail.get()) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				int currHead = atomicHead.get();
				//int x = buffer[Math.floorMod(currHead, bufSize)];
				int x = buffer[currHead % bufSize];
				finalResult += x;
				numTrue++;
				atomicHead.set(currHead + 1);
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	public class ProducerQueueAtomicLazy implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueAtomicLazy(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currTail = atomicTail.get();
				int startIndex = currTail - bufSize;
				while (atomicHead.get() <= startIndex) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				//buffer[Math.floorMod(currTail, bufSize)] = i;
				buffer[(currTail % bufSize)] = i;
				atomicTail.lazySet(currTail + 1);
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueAtomicLazy implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueAtomicLazy(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				while (atomicHead.get() >= atomicTail.get()) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				int currHead = atomicHead.get();
				//int x = buffer[Math.floorMod(currHead, bufSize)];
				int x = buffer[currHead % bufSize];
				finalResult += x;
				numTrue++;
				atomicHead.lazySet(currHead + 1);
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	public class ProducerQueueAtomicLazyMask implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueAtomicLazyMask(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currTail = atomicTail.get();
				int startIndex = currTail - bufSize;
				while (atomicHead.get() <= startIndex) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				buffer[currTail & mask] = i;
				atomicTail.lazySet(currTail + 1);
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueAtomicLazyMask implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueAtomicLazyMask(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				while (atomicHead.get() >= atomicTail.get()) {
					Thread.yield();
					/*
					 * counter++; if(counter > 1000) { Thread.yield(); counter = 0; }
					 */
				}
				int currHead = atomicHead.get();
				int x = buffer[currHead & mask];
				finalResult += x;
				numTrue++;
				atomicHead.lazySet(currHead + 1);
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	private static final int yieldLimit = 0;

	public class ProducerQueueAtomicLazyMaskYield implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueAtomicLazyMaskYield(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				int currTail = atomicTail.get();
				int startIndex = currTail - bufSize;
				while (atomicHead.get() <= startIndex) {
						Thread.yield();
				}
				buffer[currTail & mask] = i;
				atomicTail.lazySet(currTail + 1);
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueAtomicLazyMaskYield implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueAtomicLazyMaskYield(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				while (atomicHead.get() >= atomicTail.get()) {
					Thread.yield();
				}
				int currHead = atomicHead.get();
				int x = buffer[currHead & mask];
				finalResult += x;
				numTrue++;
				atomicHead.lazySet(currHead + 1);
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	public class ProducerQueueLinkedBlocking implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueLinkedBlocking(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				while (!(lbq.offer(i))) {
						Thread.yield();
				}
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueLinkedBlocking implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueLinkedBlocking(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				Integer it;
				while ((it = lbq.poll()) == null) {
					Thread.yield();
				}
				finalResult += it;
				numTrue++;
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	public class ProducerQueueConcurrentLinked implements Callable<Result> {
		private int reps;
		public long time;

		public ProducerQueueConcurrentLinked(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				while (!(clq.offer(i))) {
						Thread.yield();
				}
			});

			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, time, -1, -1);
			return r;
		}
	}

	public class ConsumerQueueConcurrentLinked implements Callable<Result> {
		private int reps;
		public long time;
		public long numTrue;

		public ConsumerQueueConcurrentLinked(int reps) {
			this.reps = reps;
		}

		private int counter;

		public Result call() {
			Instant start = Instant.now();
			IntStream.range(0, reps).forEach(i -> {
				counter = 0;
				Integer it;
				while ((it = clq.poll()) == null) {
					Thread.yield();
				}
				finalResult += it;
				numTrue++;
			});
			time = start.until(Instant.now(), ChronoUnit.MILLIS);
			Result r = new Result("", reps, -1, time, numTrue, atomicHead.get(), atomicTail.get(), finalResult);
			return r;
		}
	}

	public static class Result {
		public String name;
		public long reps;
		public long prodTime;
		public long consTime;
		public long numTrue;
		public long head;
		public long tail;
		public long finalResult;
		public List<Integer> seen;

		public Result(String name, long reps, long prodTime, long consTime, long numTrue, long head, long tail,
				long finalResult) {
			this(name, reps, prodTime, consTime, numTrue, head, tail, finalResult, null);
			
		}
		public Result(String name, long reps, long prodTime, long consTime, long numTrue, long head, long tail,
				long finalResult, List<Integer> seen) {
			super();
			this.name = name;
			this.prodTime = prodTime;
			this.consTime = consTime;
			this.numTrue = numTrue;
			this.reps = reps;
			this.head = head;
			this.tail = tail;
			this.finalResult = finalResult;
			this.seen = seen;
		}

		public Result(String name, long reps, long prodTime, long consTime, long numTrue) {
			this(name, reps, prodTime, consTime, numTrue, -1, -1, -1);
		}

		public String toString() {
			return String.format("Name: %s, ProdTime %,d, ConsTime %,d, NumTrue: %,d, head,tail: %d,%d fR: %d%n", name,
					prodTime, consTime, numTrue, head, tail, finalResult);

		}
	}

	public static class Stats {
		public double count;
		public double totalReps;
		public long totalProdTime;
		public long maxProdTime = Integer.MIN_VALUE;
		public long minProdTime = Integer.MAX_VALUE;
		public double avgProdTimePerRep;
		public double avgProdTimeTotal;
		public double prodsPerSecond;

		public long totalConsTime;
		public long maxConsTime = Integer.MIN_VALUE;
		public long minConsTime = Integer.MAX_VALUE;
		public double avgConsTimePerRep;
		public double avgConsTimeTotal;
		public double consPerSecond;

		public long totalNumTrue;
		public double avgNumTrue;

		public long head;
		public long tail;
		public long finalResult;
		
		public List<Integer> seen;

		public static void accumulate(Stats us, Result result) {
			us.count++;
			us.totalReps += result.reps;

			us.maxProdTime = Math.max(us.maxProdTime, result.prodTime);
			us.minProdTime = Math.min(us.minProdTime, result.prodTime);
			us.totalProdTime += result.prodTime;
			// us.avgProdTime = us.totalProdTime / us.totalReps;
			// us.prodsPerSecond = 1000 * us.totalReps / us.totalProdTime;

			us.maxConsTime = Math.max(us.maxConsTime, result.consTime);
			us.minConsTime = Math.min(us.minConsTime, result.consTime);
			us.totalConsTime += result.consTime;
			// us.avgConsTime = us.totalConsTime / us.totalReps;
			// us.consPerSecond = 1000 * us.totalReps / us.totalConsTime;

			us.totalNumTrue += result.numTrue;
			// us.avgNumTrue = us.totalNumTrue / us.totalReps;

			us.head = Math.max(us.head, result.head);
			us.tail = Math.max(us.tail, result.tail);
			us.finalResult = Math.max(us.finalResult, result.finalResult);
			
			us.seen = result.seen;
		}

		public static Stats combine(Stats us, Stats other) {
			us.count += other.count;
			us.totalReps += other.totalReps;

			us.maxProdTime = Math.max(us.maxProdTime, other.maxProdTime);
			us.minProdTime = Math.min(us.minProdTime, other.minProdTime);
			us.totalProdTime += other.totalProdTime;
			// us.avgProdTime = us.totalProdTime / us.count;
			// us.prodsPerSecond = 1000 * us.totalReps / us.totalProdTime;

			us.maxConsTime = Math.max(us.maxConsTime, other.maxConsTime);
			us.minConsTime = Math.min(us.minConsTime, other.minConsTime);
			us.totalConsTime += other.maxConsTime;
			// us.avgConsTime = us.totalConsTime / us.count;
			// us.consPerSecond = 1000 * us.totalReps / us.totalConsTime;

			us.totalNumTrue += other.totalNumTrue;
			// us.avgNumTrue = us.totalNumTrue / us.totalReps;

			return us;
		}

		public static Stats finish(Stats finalResult) {
			// Calculate averages at the end
			finalResult.avgProdTimePerRep = finalResult.totalProdTime / finalResult.count;
			finalResult.avgProdTimeTotal = finalResult.totalProdTime / finalResult.totalReps;

			finalResult.prodsPerSecond = 1000 * finalResult.totalReps / finalResult.totalProdTime;

			finalResult.avgConsTimePerRep = finalResult.totalConsTime / finalResult.count;
			finalResult.avgConsTimeTotal = finalResult.totalConsTime / finalResult.totalReps;
			finalResult.consPerSecond = 1000 * finalResult.totalReps / finalResult.totalConsTime;

			finalResult.avgNumTrue = finalResult.totalNumTrue / finalResult.totalReps;

			return finalResult;
		}

		@Override
		public String toString() {
			String s = String.format(
					"Stats [reps=%,.2f, totalProdTime=%,d, maxProdTime=%,d,  minProdTime=%,d, "
							+ "avgProdTimePerRep=%,f, avgProdTimeTotal=%,f%nprodsPerSecond=%,.2f"
							+ "%ntotalConsTime=%,d, maxConsTime=%,d, minConsTime=%,d "
							+ ", avgConsTimePerRep=%,f, avgConsTimeTotal=%,f%n consPerSecond=%,.2f"
							+ "%n totalNumTrue=%,d, avgNumTrue=%,f" + ", totalReps=%,.2f ]",
					count, totalProdTime, maxProdTime, minProdTime, avgProdTimePerRep, avgProdTimeTotal, prodsPerSecond,
					totalConsTime, maxConsTime, minConsTime, avgConsTimePerRep, avgConsTimeTotal, consPerSecond,
					totalNumTrue, avgNumTrue, totalReps);

			return s;
			/*
			 * return "Stats [reps=" + count + ", totalProdTime=" + totalProdTime +
			 * ", maxProdTime=" + maxProdTime + " minProdTime=" + minProdTime +
			 * ", avgProdTime=" + avgProdTime + "\n prodsPerSecond=" + prodsPerSecond +
			 * "\n totalConsTime=" + totalConsTime + ", maxConsTime=" + maxConsTime +
			 * ", minConsTime=" + minConsTime + ", avgConsTime=" + avgConsTime +
			 * "\n consPerSecond=" + consPerSecond + "\n totalNumTrue=" + totalNumTrue +
			 * ", avgNumTrue=" + avgNumTrue + ", totalReps=" + totalReps + "]";
			 */
		}
	}
}
