package ai.libs.jaicore.basic.algorithm;

import org.api4.java.algorithm.IAlgorithm;

/**
 * Event that an algorithm has been interrupted.
 *
 * @author Felix Mohr
 *
 */
public class AlgorithmInterruptedEvent extends AAlgorithmEvent {

	public AlgorithmInterruptedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}

}
