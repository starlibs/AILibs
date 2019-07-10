package ai.libs.jaicore.logic.fol.util;

import org.api4.java.algorithm.events.AAlgorithmEvent;

/**
 * This is used if a binding is found that is ok for positive literals, but negative literals of the conclusions are in the factbase.
 *
 */
public class ForwardChainingFailedCWABindingEvent extends AAlgorithmEvent {

	public ForwardChainingFailedCWABindingEvent(final String algorithmId) {
		super(algorithmId);
	}

}
