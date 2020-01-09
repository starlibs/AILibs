package ai.libs.jaicore.basic.algorithm;

import org.api4.java.algorithm.IAlgorithm;

/**
 * Event that an algorithm has finished
 *
 * @author Felix Mohr
 *
 */
public class AlgorithmFinishedEvent extends AAlgorithmEvent {

	public AlgorithmFinishedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
