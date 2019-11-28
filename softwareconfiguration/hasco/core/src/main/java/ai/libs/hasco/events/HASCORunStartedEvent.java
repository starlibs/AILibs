package ai.libs.hasco.events;

import org.api4.java.algorithm.events.AlgorithmInitializedEvent;
import org.api4.java.common.attributedobjects.IObjectEvaluator;

public class HASCORunStartedEvent<T, V extends Comparable<V>> extends AlgorithmInitializedEvent {
	private final int seed, timeout, numberOfCPUS;
	private IObjectEvaluator<T, V> benchmark;

	public HASCORunStartedEvent(final String algorithmId, final int seed, final int timeout, final int numberOfCPUS, final IObjectEvaluator<T, V> benchmark) {
		super(algorithmId);
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
