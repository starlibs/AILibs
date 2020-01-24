package ai.libs.hasco.events;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.common.attributedobjects.IObjectEvaluator;

import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;

public class HASCORunStartedEvent<T, V extends Comparable<V>> extends AlgorithmInitializedEvent {
	private final int seed;
	private final int timeout;
	private final int numberOfCPUS;
	private IObjectEvaluator<T, V> benchmark;

	public HASCORunStartedEvent(final IAlgorithm<?, ?> algorithm, final int seed, final int timeout, final int numberOfCPUS, final IObjectEvaluator<T, V> benchmark) {
		super(algorithm);
		this.seed = seed;
		this.timeout = timeout;
		this.numberOfCPUS = numberOfCPUS;
		this.benchmark = benchmark;
	}

	public IObjectEvaluator<T, V> getBenchmark() {
		return this.benchmark;
	}

	public void setBenchmark(final IObjectEvaluator<T, V> benchmark) {
		this.benchmark = benchmark;
	}

	public int getSeed() {
		return this.seed;
	}

	public int getTimeout() {
		return this.timeout;
	}

	public int getNumberOfCPUS() {
		return this.numberOfCPUS;
	}

}
