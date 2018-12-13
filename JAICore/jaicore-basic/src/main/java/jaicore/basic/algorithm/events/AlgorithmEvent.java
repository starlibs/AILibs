package jaicore.basic.algorithm.events;

public interface AlgorithmEvent {
	
	public String getAlgorithmId(); // the id of the algorithm that has issued this event
	
	public long getTimestamp(); // the time when this event has occurred
}
