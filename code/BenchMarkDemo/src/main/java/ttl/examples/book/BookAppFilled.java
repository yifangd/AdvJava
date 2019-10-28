package ttl.examples.book;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BookAppFilled {

	public static void main(String[] args) throws IOException {
		ExecutorService eService = Executors.newFixedThreadPool(8);
		Map<String, Long> cwo = countWordsTheOldWay("PrideAndPrejudice.txt");
		Map<String, Long> cwomerge = countWordsTheOldWayMerge("PrideAndPrejudice.txt");
		Map<String, Long> cwom = countWordsTheOldWayWithMatcher("PrideAndPrejudice.txt");
		Map<String, Long> cwomcp = countWordsTheOldWayWithMatcherCustomParallel("PrideAndPrejudice.txt", eService);
		Map<String, Long> cw = countWordsStream("PrideAndPrejudice.txt");
		Map<String, Long> cwm = countWordsStreamMatcher("PrideAndPrejudice.txt");
		Map<String, Long> cwcm = countWordsStreamCustomMatcher("PrideAndPrejudice.txt");
		Map<String, Long> cwm9 = countWordsStreamMatcherJdk9("PrideAndPrejudice.txt");
		Map<String, Long> cwp = countWordsParallel("PrideAndPrejudice.txt");

		System.out.printf(
				"cwo.size = %d, " + "cwom.size = %d, cwommerge.size = %d, " + "cwomcp.size = %d " + "cw.size = %d " + "cwm.size = %d " + "cwcm.size = %d "
						+ "cwm9.size = %d " + "cwp.size = %d " + "%n",
				cwo.size(), cwom.size(), cwomerge.size(), cwomcp.size(), cw.size(), cwm.size(), cwcm.size(), cwm9.size(), cwp.size());
		// cwm.forEach((k, v) -> System.out.printf("%s = %d%n", k, v));
		System.out.printf(
				"cwo.totCount = %d, " + "cwom.totCount = %d, cwomerge.count = %d, " + "cwomcp.totCount = %d " + "cw.totCount = %d " + "cwm.totCount = %d " + "cwcm.totCount = %d "
						+ "cwm9.totCount = %d " + "cwp.totCount = %d " + "%n",
				cwo.values().stream().mapToLong(Long::longValue).sum(), 
				cwom.values().stream().mapToLong(Long::longValue).sum(), 
				cwomerge.values().stream().mapToLong(Long::longValue).sum(), 
				cwomcp.values().stream().mapToLong(Long::longValue).sum(), 
				cw.values().stream().mapToLong(Long::longValue).sum(), 
				cwm. values().stream().mapToLong(Long::longValue).sum(),
				cwcm. values().stream().mapToLong(Long::longValue).sum(),
				cwm9.values().stream().mapToLong(Long::longValue).sum(),
				cwp.values().stream().mapToLong(Long::longValue).sum());
		eService.shutdown();
	}

	public static Map<String, Long> countWordsTheOldWay(String fileName) throws IOException {
		// List<String> lines = Files.readAllLines(Paths.get(fileName));

		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

			Map<String, Long> result = new ConcurrentHashMap<>();
			String line;

			while ((line = reader.readLine()) != null) {
				String[] words = line.split("\\W");

				for (String word : words) {
					if (!word.matches("\\s*")) {
					//result.merge(word, 1L, (orig, current) -> current + 1);
						long count = result.computeIfAbsent(word, (s -> 0L));
						count++;
						result.put(word, count);
					}
				}
			}
			return result;
		}

		// result.forEach((key, value) -> System.out.println("Word: " + key + ",
		// Count:" + value));
	}

	public static Map<String, Long> countWordsTheOldWayMerge(String fileName) throws IOException {
		// List<String> lines = Files.readAllLines(Paths.get(fileName));

		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

			Map<String, Long> result = new ConcurrentHashMap<>();
			String line;

			while ((line = reader.readLine()) != null) {
				String[] words = line.split("\\W");

				for (String word : words) {
					if (!word.matches("\\s*")) {
						result.merge(word, 1L, (orig, current) -> orig + current);
					}
				}
			}
			return result;
		}

		// result.forEach((key, value) -> System.out.println("Word: " + key + ",
		// Count:" + value));
	}

	public static Map<String, Long> countWordsTheOldWayWithMatcher(String fileName) throws IOException {
		// List<String> lines = Files.readAllLines(Paths.get(fileName));
		Pattern wordRE = Pattern.compile("\\w+");

		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

			Map<String, Long> result = new ConcurrentHashMap<>();
			String line;

			while ((line = reader.readLine()) != null) {
				Matcher matcher = wordRE.matcher(line);
				matcher.reset();
				while (matcher.find()) {
					String word = matcher.group();
					result.merge(word, 1L, (orig, current) -> current + orig);
					/*
					long count = result.computeIfAbsent(word, (s -> 0L));
					count++;
					result.put(word, count);
					*/
				}
			}
			return result;
		}
	}


	public static Map<String, Long> countWordsTheOldWayWithMatcherCustomParallel(String fileName,
			ExecutorService eService) throws IOException {
		// List<String> lines = Files.readAllLines(Paths.get(fileName));
		Pattern wordRE = Pattern.compile("\\w+");

		try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {

			Map<String, Long> result = new ConcurrentHashMap<>();
			String line;

			List<Future<?>> futures = new ArrayList<>();
			while ((line = reader.readLine()) != null) {
				Future<?> f = eService.submit(makeRunner(line, wordRE, result));
				futures.add(f);
			}
			
			futures.forEach(f -> {
				try {
					f.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			});

			return result;
		}
	}

	private static Runnable makeRunner(String line, Pattern wordRE, Map<String, Long> result) {
		Runnable r = () -> {
			Matcher matcher = wordRE.matcher(line);
			matcher.reset();
			while (!Thread.currentThread().isInterrupted() && matcher.find()) {
				String word = matcher.group();
					result.merge(word, 1L, (orig, current) -> current + orig);
					/*
				long count = result.computeIfAbsent(word, (s -> 0L));
				count++;
				result.put(word, count);
				*/
			}
		};
		
		return r;
	}

	public static Map<String, Long> countWordsStream(String fileName) throws IOException {
		Map<String, Long> result = Files.lines(Paths.get(fileName)).flatMap(s -> Arrays.stream(s.split("\\W")))
				.filter(s -> !s.matches("\\s*"))
				.collect(Collectors.groupingBy(s -> s, ConcurrentHashMap::new, Collectors.counting()));

		return result;
	}

	public static Map<String, Long> countWordsStreamMatcher(String fileName) throws IOException {
		Pattern wordRE = Pattern.compile("\\w+");
		Map<String, Long> result = Files.lines(Paths.get(fileName)).flatMap(line -> {
			Matcher matcher = wordRE.matcher(line);
			List<String> list = new ArrayList<>();
			matcher.reset();
			while (matcher.find()) {
				String word = matcher.group();
				list.add(word);
			}
			return list.stream();
		}).filter(s -> !s.matches("\\s*")).collect(Collectors.groupingByConcurrent(s -> s, 
				ConcurrentHashMap::new, Collectors.counting()));
		return result;
	}

	public static Map<String, Long> countWordsStreamCustomMatcher(String fileName) throws IOException {
		Pattern wordRE = Pattern.compile("\\w+");
		Map<String, Long> result = Files.lines(Paths.get(fileName)).flatMap(line -> {
			Matcher matcher = wordRE.matcher(line);
			matcher.reset();
			return MatcherSpliterator.stream(matcher);
		//}).map(mr -> mr.group()).filter(s -> !s.matches("\\s*"))
		}).filter(s -> !s.matches("\\s*"))
				.collect(Collectors.groupingBy(s -> s, ConcurrentHashMap::new, Collectors.counting()));
		return result;
	}

	/**
	 * Jdk 9+ only - for matcher.results()
	 *
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Long> countWordsStreamMatcherJdk9(String fileName) throws IOException {
		Pattern wordRE = Pattern.compile("\\w+");
		Map<String, Long> result = Files.lines(Paths.get(fileName))
				// .parallel()
				.flatMap(line -> wordRE.matcher(line).results()).map(matchResult -> matchResult.group())
				.filter(s -> !s.matches("\\s*"))
				.collect(Collectors.groupingBy(s -> s, ConcurrentHashMap::new, Collectors.counting()));
		// .collect(Collectors.groupingByConcurrent(s -> s, ConcurrentHashMap::new,
		// Collectors.counting()));

		return result;
	}

	public static Map<String, Long> countWordsParallel(String fileName) throws IOException {
		Map<String, Long> result = Files.lines(Paths.get(fileName)).parallel()
				.flatMap(s -> Arrays.stream(s.split("\\W"))).filter(s -> !s.matches("\\s*"))
				.collect(Collectors.groupingBy(s -> s, ConcurrentHashMap::new, Collectors.counting()));

		return result;
	}

	public static Map<String, Long> countWordsParallelConcurrent(String fileName) throws IOException {
		Map<String, Long> result = Files.lines(Paths.get(fileName)).parallel()
				.flatMap(s -> Arrays.stream(s.split("\\W"))).filter(s -> !s.matches("\\s*"))
				.collect(Collectors.groupingByConcurrent(s -> s, ConcurrentHashMap::new, Collectors.counting()));

		return result;
	}
}
