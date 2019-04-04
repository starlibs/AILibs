package jaicore.graphvisualizer.events.controlEvents;

/**
 * A ControlEvent which is used to propagate through the search. It is possible
 * to go forward and also backwards.
 * 
 *
 */
public class StepEvent implements ControlEvent {
	/**
	 * a boolean which indicated the direction of the event
	 */
	private boolean forward;
	/**
	 * a integer which indicated how many steps are done at a time
	 */
	private int steps;

	public StepEvent(boolean forward, int steps) {
		this.forward = forward;
		this.steps = steps;
	}

	public boolean forward() {
		return forward;
	}

	public int getSteps() {
		return steps;
	}
}
