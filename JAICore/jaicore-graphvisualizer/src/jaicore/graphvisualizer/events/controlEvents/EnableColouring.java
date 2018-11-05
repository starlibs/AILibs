package jaicore.graphvisualizer.events.controlEvents;

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
