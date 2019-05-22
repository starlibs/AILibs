package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.events.AAlgorithmEvent;

public class FValueEvent<V> extends AAlgorithmEvent {

	private V fValue;

	private double timeUnitlFound;

	public FValueEvent(String algorithmId, V fValue, double timeUnitlFound) {
		super(algorithmId);
		this.fValue = fValue;
		this.timeUnitlFound = timeUnitlFound;
	}

	public V getfValue() {
		return fValue;
	}

	public double getTimeUnitlFound() {
		return timeUnitlFound;
	}
}
