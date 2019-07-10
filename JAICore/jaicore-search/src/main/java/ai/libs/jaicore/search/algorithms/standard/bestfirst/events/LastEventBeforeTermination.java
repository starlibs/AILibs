package ai.libs.jaicore.search.algorithms.standard.bestfirst.events;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class LastEventBeforeTermination extends AAlgorithmEvent {

	public LastEventBeforeTermination(final String algorithmId) {
		super(algorithmId);
	}

}
