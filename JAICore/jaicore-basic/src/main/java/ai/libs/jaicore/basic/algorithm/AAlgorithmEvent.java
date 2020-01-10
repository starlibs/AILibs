package ai.libs.jaicore.basic.algorithm;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;

/**
 * Simple implementation of an algorithm event that takes the current time as the time stamp.
 *
 * @author Felix Mohr
 *
 */
public class AAlgorithmEvent implements IAlgorithmEvent {

	private final long timestamp = System.currentTimeMillis();
	private final String algorithmId;

	/**
	 * @param algorithm The algorithm to which this event is related.
	 */
	public AAlgorithmEvent(final IAlgorithm<?, ?> algorithm) {
		super();
		this.algorithmId = algorithm != null ? algorithm.getId() : "<unknown algorithm>";
	}

	@Override
	public String getAlgorithmId() {
		return this.algorithmId;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

}
