package ai.libs.mlplan.cli.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.StatisticsUtil;
import ai.libs.mlplan.core.events.ClassifierFoundEvent;

public class StatisticsListener {

	private AtomicInteger modelsEvaluatedCounter;
	private Map<String, List<Double>> rootLearnerStatistics;
	private Lock lock = new ReentrantLock();

	public StatisticsListener() {
		this.modelsEvaluatedCounter = new AtomicInteger(0);
		this.rootLearnerStatistics = new HashMap<>();
	}

	@Subscribe
	public void rcvClassifierFoundEvent(final ClassifierFoundEvent e) {
		this.modelsEvaluatedCounter.incrementAndGet();

		this.lock.lock();
		try {
			this.rootLearnerStatistics.computeIfAbsent(e.getComponentDescription().getComponent().getName(), t -> new ArrayList<>()).add(e.getScore());
		} finally {
			this.lock.unlock();
		}
	}

	public int getNumModelsEvaluated() {
		return this.modelsEvaluatedCounter.get();
	}

	public Map<String, Map<String, Double>> getRootLearnerStatistics() {
		Map<String, Map<String, Double>> result = new HashMap<>();

		for (Entry<String, List<Double>> entry : this.rootLearnerStatistics.entrySet()) {
			Map<String, Double> stats = new HashMap<>();
			stats.put("n", (double) entry.getValue().size());
			stats.put("max_score", StatisticsUtil.max(entry.getValue()));
			stats.put("min_score", StatisticsUtil.min(entry.getValue()));
			stats.put("mean_score", StatisticsUtil.mean(entry.getValue()));
			stats.put("median_score", StatisticsUtil.median(entry.getValue()));
			stats.put("score_variance", StatisticsUtil.variance(entry.getValue()));
			stats.put("score_standarddeviation", StatisticsUtil.standardDeviation(entry.getValue()));
			result.put(entry.getKey(), stats);
		}
		return result;
	}

}
