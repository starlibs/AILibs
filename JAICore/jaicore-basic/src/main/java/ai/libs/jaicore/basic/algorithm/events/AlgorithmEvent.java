package ai.libs.jaicore.basic.algorithm.events;

import ai.libs.jaicore.basic.events.IEvent;

public interface AlgorithmEvent extends IEvent {

	public String getAlgorithmId(); // the id of the algorithm that has issued this event

	public long getTimestamp(); // the time when this event has occurred
}
