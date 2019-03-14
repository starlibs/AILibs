package jaicore.search.algorithms.standard.bestfirst.events;

import jaicore.basic.algorithm.events.AlgorithmEvent;

public class FValueEvent<V> implements AlgorithmEvent{

	private V fValue;
	
	private double timeUnitlFound;

	public FValueEvent(V fValue, double timeUnitlFound) {
		super();
		this.fValue = fValue;
		this.timeUnitlFound = timeUnitlFound;
	}

	public V getfValue() {
		return fValue;
	}

	public double getTimeUnitlFound() {
		return timeUnitlFound;
	}

	@Override
	public String getAlgorithmId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimestamp() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
