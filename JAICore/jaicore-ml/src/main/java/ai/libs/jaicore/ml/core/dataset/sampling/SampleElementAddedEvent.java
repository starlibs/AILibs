package ai.libs.jaicore.ml.core.dataset.sampling;

import org.api4.java.algorithm.events.AAlgorithmEvent;

public class SampleElementAddedEvent extends AAlgorithmEvent {

	public SampleElementAddedEvent(final String algorithmId) {
		super(algorithmId);
	}

}
