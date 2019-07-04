package ai.libs.jaicore.logic.fol.util;

import ai.libs.jaicore.basic.algorithm.events.AAlgorithmEvent;

/**
 * This is used if a binding is found that is ok for positive literals, but negative literals of the conclusions are in the factbase.
 *
 */
public class ForwardChainingFailedCWABindingEvent extends AAlgorithmEvent {

	public ForwardChainingFailedCWABindingEvent(String algorithmId) {
		super(algorithmId);
	}

}
