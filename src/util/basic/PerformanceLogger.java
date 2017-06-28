package util.basic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class PerformanceLogger {

	public static enum PerformanceMeasure {
		TIME, MEMORY_KB, MEMORY_MB
	};

	private static PerformanceLogger singleton = new PerformanceLogger();
	private static Runtime runtime = Runtime.getRuntime();

	private PerformanceLogger() {

	}

	public static PerformanceLogger getPrivatePerformanceLogger() {
		return new PerformanceLogger();
	}

	public static PerformanceLogger getInstance() {
		return singleton;
	}

	private Map<String, PerformanceLog> openLog = new HashMap<>();
	private List<PerformanceLog> closedLog = new ArrayList<>();
	private final Lock openLogLock = new ReentrantLock();
	private final Lock closedLogLock = new ReentrantLock();

	public static void logStart() {
		singleton.start();
	}

	public static void logStart(final String tag) {
		singleton.start(tag);
	}

	public static void logEnd() {
		singleton.end();
	}

	public static void logEnd(final String tag) {
		singleton.end(tag);
	}

	public static List<PerformanceLog> getPerformanceLog() {
		return singleton.closedLog;
	}

	public void start() {
		final StackTraceElement[] elemArray = Thread.currentThread().getStackTrace();
		final StackTraceElement caller = elemArray[2];

		final String identString = Thread.currentThread().getName() + ":" + caller.getClassName() + caller.getMethodName();
		this.start(identString);
	}

	public void start(final String tag) {
		final StackTraceElement[] elemArray = Thread.currentThread().getStackTrace();
		final StackTraceElement caller = elemArray[2];

		final String identString = tag;
		final PerformanceLog log = singleton.new PerformanceLog(
				new LogEntry(tag, Thread.currentThread().getName(), caller, System.currentTimeMillis(), (runtime.totalMemory() - runtime.freeMemory()) / 1024));

		this.openLogLock.lock();
		try {
			this.openLog.put(identString, log);
		} finally {
			this.openLogLock.unlock();
		}
	}

	public void end() {
		final StackTraceElement[] elemArray = Thread.currentThread().getStackTrace();
		final StackTraceElement caller = elemArray[2];
		final String identString = Thread.currentThread().getName() + ":" + caller.getClassName() + caller.getMethodName();
		this.end(identString);
	}

	public void end(final String tag) {
		final StackTraceElement[] elemArray = Thread.currentThread().getStackTrace();
		final StackTraceElement caller = elemArray[2];
		final String identString = tag;
		PerformanceLog log;
		this.openLogLock.lock();
		try {
			log = this.openLog.get(identString);
			this.openLog.remove(log);
		} finally {
			this.openLogLock.unlock();
		}
		if (log != null) {

			log.setEnd(new LogEntry(tag, Thread.currentThread().getName(), caller, System.currentTimeMillis(), (runtime.totalMemory() - runtime.freeMemory()) / 1024));
			this.closedLogLock.lock();
			try {
				this.closedLog.add(log);
			} finally {
				this.closedLogLock.unlock();
			}
		}
	}

	public List<String> getRegisteredTags() {
		final Set<String> tags = new HashSet<>();
		for (final PerformanceLog log : this.closedLog) {
			tags.add(log.start.tag);
		}
		return new ArrayList<>(tags);
	}

	public List<PerformanceLog> performanceLog() {
		return this.closedLog;
	}

	public List<PerformanceLog> getPerformanceLog(final String tag) {
		final List<PerformanceLog> out = new ArrayList<>();
		for (final PerformanceLog log : this.closedLog) {
			if (log.start.tag.equals(tag)) {
				out.add(log);
			}
		}
		return out;
	}

	public static void printPerformanceLog() {
		printPerformanceLog(singleton);
	}

	public static void printPerformanceLog(final PerformanceLogger instance) {
		for (final PerformanceLog log : instance.closedLog) {
			System.out.println(log);
		}
	}

	public static Map<String, List<PerformanceLog>> getPerformanceLogByTag(final PerformanceLogger instance) {
		final Map<String, List<PerformanceLog>> map = new HashMap<>();
		for (final PerformanceLog log : instance.closedLog) {
			if (!map.containsKey(log.start.tag)) {
				map.put(log.start.tag, new ArrayList<>());
			}
			map.get(log.start.tag).add(log);
		}
		return map;
	}

	public static Map<String, Integer> getAveragePerformancePerTag(final PerformanceLogger instance) {
		final Map<String, List<PerformanceLog>> performanceByTag = getPerformanceLogByTag(instance);
		final Map<String, Integer> out = new HashMap<>();
		for (final String tag : performanceByTag.keySet()) {
			int total = 0;
			int i = 0;
			for (final PerformanceLog log : performanceByTag.get(tag)) {
				total += (log.end.timestamp - log.start.timestamp);
				i++;
			}
			out.put(tag, total / i);
		}
		return out;
	}

	public static Map<String, PerformanceStats> getPerformanceStatsPerTag(final PerformanceLogger instance, final PerformanceMeasure measure) {
		final Map<String, List<PerformanceLog>> performanceByTag = getPerformanceLogByTag(instance);
		final Map<String, PerformanceStats> out = new HashMap<>();
		for (final String tag : performanceByTag.keySet()) {
			out.put(tag, instance.getPerformanceStatsPerTag(tag, measure));
		}
		return out;
	}

	public PerformanceStats getPerformanceStatsPerTag(final String tag, final PerformanceMeasure measure) {
		final List<PerformanceLog> performance = this.getPerformanceLog(tag);
		int min = Integer.MAX_VALUE;
		int max = 0;
		int total = 0;

		List<PerformanceLog> sortedLogs = null;
		if (measure == PerformanceMeasure.TIME) {
			sortedLogs = performance.stream().sorted((l1, l2) -> ((int) ((l1.end.timestamp - l1.start.timestamp) - (l2.end.timestamp - l2.start.timestamp))))
					.collect(Collectors.toList());
		} else if (measure == PerformanceMeasure.MEMORY_KB || measure == PerformanceMeasure.MEMORY_MB) {
			sortedLogs = performance.stream().sorted((l1, l2) -> ((int) ((l1.end.memoryConsumed - l1.start.memoryConsumed) - (l2.end.memoryConsumed - l2.start.memoryConsumed))))
					.collect(Collectors.toList());
		}

		final int n = sortedLogs.size();
		if (n == 0) {
			return null;
		}
		int i = 1;
		int quart25 = -1, quart50 = -1, quart75 = -1;
		for (final PerformanceLog log : sortedLogs) {
			int val = (int) ((measure == PerformanceMeasure.TIME ? (log.end.timestamp - log.start.timestamp) : (log.end.memoryConsumed - log.start.memoryConsumed)));
			if (measure == PerformanceMeasure.MEMORY_MB) {
				val /= 1024;
			}
			total += val;
			min = Math.min(min, val);
			max = Math.max(max, val);
			if ((1f * i / n > 0.25) && (quart25 == -1)) {
				quart25 = val;
			}
			if ((1f * i / n > 0.5) && (quart50 == -1)) {
				quart50 = val;
			}
			if ((1f * i / n > 0.75) && (quart75 == -1)) {
				quart75 = val;
			}
			i++;
		}
		return new PerformanceStats(n, min, max, quart25, quart50, quart75, total / n, total);

	}

	public void printStats(final String tag, final PerformanceMeasure measure) {
		final PerformanceStats stats = this.getPerformanceStatsPerTag(tag, measure);
		if (stats != null) {
			if (measure == PerformanceMeasure.TIME) {
				System.out.println("Stats for tag [" + tag + "]: " + stats.n + " run(s) with total time " + stats.total + "ms (Average per log is " + stats.mean
						+ "ms. Boxplot (min,0.25quart,median,0.75quart,max): " + stats.min + "/" + stats.quart25 + "/" + stats.median + "/" + stats.quart75 + "/" + stats.max
						+ ")");
			} else if (measure == PerformanceMeasure.MEMORY_KB || measure == PerformanceMeasure.MEMORY_MB) {
				System.out.println("Stats for tag [" + tag + "]: " + stats.n + " run(s) with total memory " + stats.total + (measure == PerformanceMeasure.MEMORY_KB ? "KB" : "MB")
						+ " (Average per log is " + stats.mean + (measure == PerformanceMeasure.MEMORY_KB ? "KB" : "MB") + ". Boxplot (min,0.25quart,median,0.75quart,max): "
						+ stats.min + "/" + stats.quart25 + "/" + stats.median + "/" + stats.quart75 + "/" + stats.max + ")");
			} else {
				System.err.println("Cannot output log stats for performance measure " + measure + " since the output is not implemented.");
			}
		} else {
			System.out.println(stats);
		}
	}

	public static void printStatsPerTag(final PerformanceLogger instance, final PerformanceMeasure measure) {
		final Map<String, PerformanceStats> statsPerTag = getPerformanceStatsPerTag(instance, measure);
		for (final String tag : statsPerTag.keySet()) {
			instance.printStats(tag, measure);
		}
	}

	public void printStats(final PerformanceMeasure measure) {
		for (final String tag : this.getRegisteredTags()) {
			this.printStats(tag, measure);
		}
	}

	public static void printAndClear(final PerformanceMeasure measure) {
		printPerformanceLog(singleton);
		printStatsPerTag(singleton, measure);
		clearLog(singleton);
	}

	public static void printAndClear(final PerformanceLogger instance, final PerformanceMeasure measure) {
		printPerformanceLog(instance);
		printStatsPerTag(instance, measure);
		clearLog(instance);
	}

	public static void printLogAndClear() {
		printPerformanceLog(singleton);
		clearLog(singleton);
	}

	public static void printLogAndClear(final PerformanceLogger instance) {
		printPerformanceLog(instance);
		clearLog(instance);
	}

	public void printAndClear(final String tag, final PerformanceMeasure measure) {
		printPerformanceLog();
		this.printStats(tag, measure);
		clearLog();
	}

	public static void printStatsAndClear(final PerformanceMeasure measure) {
		printStatsPerTag(singleton, measure);
		clearLog(singleton);
	}

	public static void printStatsAndClear(final PerformanceLogger instance, final PerformanceMeasure measure) {
		printStatsPerTag(instance, measure);
		clearLog(instance);
	}

	public static void clearLog() {
		clearLog(singleton);
	}

	public static void clearLog(final PerformanceLogger instance) {
		instance.closedLogLock.lock();
		try {
			instance.closedLog = new LinkedList<>();
		} finally {
			instance.closedLogLock.unlock();
		}
		instance.openLogLock.lock();
		try {
			instance.openLog = new HashMap<>();
		} finally {
			instance.openLogLock.unlock();
		}
	}

	public static class LogEntry {
		private final String tag;
		private final String thread;
		private final StackTraceElement element;
		private final long timestamp;
		private final long memoryConsumed;

		private final static boolean whenTagSetShowContext = false;

		public LogEntry(final String tag, final String pThread, final StackTraceElement element, final long pTimestamp, final long pConsumedMemory) {
			this.tag = tag;
			this.thread = pThread;
			this.element = element;
			this.timestamp = pTimestamp;
			this.memoryConsumed = pConsumedMemory;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();

			sb.append("[");

			sb.append(this.tag);

			if (whenTagSetShowContext || this.tag.isEmpty()) {
				if (!this.tag.isEmpty()) {
					sb.append(">>");
				}
				sb.append(this.thread);
				sb.append(":");
				sb.append(this.element.getClassName());
				sb.append(".");
				sb.append(this.element.getMethodName());
				sb.append(":");
				sb.append(this.element.getLineNumber());
			}
			sb.append("]");

			return sb.toString();
		}
	}

	public static void clearStatsFile(final String filename) {
		try {
			final BufferedWriter w = new BufferedWriter(new FileWriter(new File(filename), false));
			w.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void writeStatsOfTagToFile(final String tag, final String filename, final boolean append) {
		final List<PerformanceLog> log = this.getPerformanceLog(tag);
		try {
			final BufferedWriter w = new BufferedWriter(new FileWriter(new File(filename), append));
			final int n = log.size();
			for (int i = 0; i < log.size(); i++) {
				final PerformanceLog l = log.get(i);
				w.write((l.end.timestamp - l.start.timestamp) + (i == n - 1 ? "" : ", "));
			}
			w.write("\n");
			w.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public class PerformanceLog {
		private final LogEntry start;
		private LogEntry end;

		public PerformanceLog(final LogEntry start) {
			this.start = start;
		}

		public void setEnd(final LogEntry end) {
			this.end = end;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append(this.start);
			sb.append(" -- ");
			sb.append(this.end.timestamp - this.start.timestamp);
			sb.append("ms --> ");
			sb.append(this.end);
			sb.append("}");
			return sb.toString();
		}
	}

	private static class PerformanceStats {
		int n, min, max, median, quart25, quart75, mean, total;

		public PerformanceStats(final int n, final int min, final int max, final int quart25, final int median, final int quart75, final int mean, final int total) {
			super();
			this.n = n;
			this.min = min;
			this.max = max;
			this.median = median;
			this.quart25 = quart25;
			this.quart75 = quart75;
			this.mean = mean;
			this.total = total;
		}

		@Override
		public String toString() {
			final String[] performanceValues = { this.n + "", this.min + "", this.max + "", this.median + "", this.quart25 + "", this.quart75 + "", this.mean + "",
					this.total + "" };
			return String.join(";", performanceValues);
		}

		public String getCSVHeader() {
			final String[] headers = { "n", "min", "max", "median", "quart25", "quart75", "mean", "total" };
			return String.join(";", headers);
		}

	}

	public static void saveGlobalLogToFile(final File logFile) {
		singleton.saveLogToFile(logFile);
	}

	public void saveLogToFile(final File logFile) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile))) {

			boolean first = true;
			for (final String tag : this.getRegisteredTags()) {
				final PerformanceStats stats = this.getPerformanceStatsPerTag(tag, PerformanceMeasure.TIME);
				if (first) {
					first = false;
					bw.write("tag;" + stats.getCSVHeader() + "\n");
				}
				bw.write(tag + ";" + stats + "\n");
			}

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
