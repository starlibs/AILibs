package jaicore.search.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.Subscribe;

import jaicore.search.structure.core.GraphEventBus;

public class Recorder<T> {
	
	
	List<Object> events;
	GraphEventBus<T> recordEventBus;
	GraphEventBus<T> playEventBus;
	
	public Recorder(GraphEventBus<T> eventBus) {
		this.recordEventBus = eventBus;
		eventBus.register(this);
		playEventBus = new GraphEventBus<>();
		events = new ArrayList<Object>();
		
	}

	
	@Subscribe
	public void receiveEvent(T event) {
		events.add(event);
		
	}
	
	public GraphEventBus<T> getEventBus() {
		return playEventBus;
	}

	public void play() {
		for(Object e : events) {
			playEventBus.post(e);
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
	}
	

}
