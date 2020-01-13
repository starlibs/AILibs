package ai.libs.jaicore.basic.algorithm;

import org.api4.java.algorithm.IAlgorithm;

/**
 * Event that an algorithm has been initialized.
 *
 * @author Felix Mohr
 */
public class AlgorithmInitializedEvent extends AAlgorithmEvent {
	public AlgorithmInitializedEvent(final IAlgorithm<?, ?> algorithm) {
		super(algorithm);
	}
}
