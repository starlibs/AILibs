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
	//time which should be waited between to outgoing events
	private int sleepTime = 10;
	//the next event to post 
	private int index;
	
	
	public Recorder(GraphEventBus<T> eventBus) {
		this.index = 0;
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
				TimeUnit.MILLISECONDS.sleep(sleepTime);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		
	}
	public void step() {
		playEventBus.post(events.get(index));
		index++;
	}
	
	
	public int getSleepTime() {
		return sleepTime;
	}


	public void setSleepTime(int sleepTime) {
		this.sleepTime = sleepTime;
	}
	

}
