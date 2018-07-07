package jaicore.graphvisualizer.gui.dataSupplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.VisuEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;

public class NodeExpansionSupplier implements ISupplier {
	
	static int i = 0;
	
	public EventBus eventbus;
	
	public NodeExpansionSupplier() {
		super();
		System.out.println(this.getClass().getSimpleName());
		this.eventbus = new EventBus();
	}

	@Override
	public void registerListener(Object listener) {
		System.out.println("register");
		eventbus.register(listener);
	}

	@Override
	@Subscribe
	public void receiveGraphEvent(VisuEvent event) {
		// TODO Auto-generated method stub
		this.eventbus.post(event);
	}

	@Override
	public void receiveControlEvent(ControlEvent event) {
	}

	@Override
	public JsonNode getSerialization() {
		// TODO Auto-generated method stub
		return null;
	}

}
