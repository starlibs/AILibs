package de.upb.crc901.mlplan.examples.multilabel.meka;

import com.google.common.eventbus.Subscribe;

/**
 * For intermediate solution evaluation. (debugging purposes)
 * 
 * @author Helena Graf
 *
 */
public class SolutionPrinter {

	/**
	 * Prints the contents of a received intermediate solution event.
	 * 
	 * @param e
	 *            the received event
	 */
	@Subscribe
	public void receiveIntermediateSolutionEvent(IntermediateSolutionEvent e) {
		System.out.println(e);
	}
}
