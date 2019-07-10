package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class FValueEvent<V> extends AAlgorithmEvent {

	private V fValue;

	private double timeUnitlFound;

	public FValueEvent(final String algorithmId, final V fValue, final double timeUnitlFound) {
		super(algorithmId);
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
