package jaicore.search.gui;

import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;

public interface ControllableSearch {
	
	@Subscribe
	default void receiveControlEvent(ControlEvent event) {
		System.out.println(event);		
	}

}
