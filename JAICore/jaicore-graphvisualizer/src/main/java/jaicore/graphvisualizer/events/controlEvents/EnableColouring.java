package jaicore.graphvisualizer.events.controlEvents;

/**
 * Event which enables or disables the node-coloring-modes
 *
 */
public class EnableColouring implements ControlEvent {

	private boolean colouring;
	
	public boolean isColouring() {
		return colouring;
	}

	/**
	 * Creates a new event 
	 * @param colour
	 */
	public EnableColouring(boolean colour) {
		this.colouring = colour;
	}
	
}
