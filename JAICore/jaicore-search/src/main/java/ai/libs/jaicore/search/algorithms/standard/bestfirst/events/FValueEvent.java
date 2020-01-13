package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.IAlgorithm;

import ai.libs.jaicore.basic.algorithm.AAlgorithmEvent;

public class FValueEvent<V> extends AAlgorithmEvent {

	private V fValue;

	private double timeUnitlFound;

	public FValueEvent(final IAlgorithm<?, ?> algorithm, final V fValue, final double timeUnitlFound) {
		super(algorithm);
		this.fValue = fValue;
		this.timeUnitlFound = timeUnitlFound;
	}

	public V getfValue() {
		return this.fValue;
	}

	public double getTimeUnitlFound() {
		return this.timeUnitlFound;
	}
}
