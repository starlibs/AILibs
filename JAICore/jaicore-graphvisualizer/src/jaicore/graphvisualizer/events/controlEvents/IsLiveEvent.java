package jaicore.graphvisualizer.events.controlEvents;

/**
 * A ControlEvent which is used as a switch between live mode and replay mode of
 * the gui
 * 
 * @author jkoepe
 *
 */

public class IsLiveEvent implements ControlEvent {

	boolean isLive;

	public IsLiveEvent(boolean isLive) {
		this.isLive = isLive;
	}

	public boolean isLive() {
		return this.isLive;
	}
}
