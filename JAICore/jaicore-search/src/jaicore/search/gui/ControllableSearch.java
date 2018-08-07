package jaicore.search.gui;

/**
 * An interface which definse a controllable search. 
 */
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;

public interface ControllableSearch {
	
	@Subscribe
	void receiveControlEvent(ControlEvent event);
}
