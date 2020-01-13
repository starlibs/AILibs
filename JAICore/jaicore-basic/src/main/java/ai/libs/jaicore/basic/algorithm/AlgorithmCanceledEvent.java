package ai.libs.jaicore.basic.algorithm;

import org.api4.java.algorithm.IAlgorithm;

/**
 * Event that an algorithm has been canceled.
 *
 * @author Felix Mohr
 *
 */
public class AlgorithmCanceledEvent extends AAlgorithmEvent {

	public AlgorithmCanceledEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
