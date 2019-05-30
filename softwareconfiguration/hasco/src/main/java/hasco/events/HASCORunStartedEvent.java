package hasco.events;

import jaicore.basic.IObjectEvaluator;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;

public class HASCORunStartedEvent<T, V extends Comparable<V>> extends AlgorithmInitializedEvent {
	private final int seed, timeout, numberOfCPUS;
	private IObjectEvaluator<T, V> benchmark;

	public HASCORunStartedEvent(String algorithmId, int seed, int timeout, int numberOfCPUS, IObjectEvaluator<T, V> benchmark) {
		super(algorithmId);
		this.seed = seed;
		this.timeout = timeout;
		this.numberOfCPUS = numberOfCPUS;
		this.benchmark = benchmark;
	}

	public IObjectEvaluator<T, V> getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(IObjectEvaluator<T, V> benchmark) {
		this.benchmark = benchmark;
	}

	public int getSeed() {
		return seed;
	}

	public int getTimeout() {
		return timeout;
	}

	public int getNumberOfCPUS() {
		return numberOfCPUS;
	}

}
