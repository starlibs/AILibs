package jaicore.search.gui;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.Subscribe;

import jaicore.search.structure.core.GraphEventBus;

public class RecordPlayer<T> {
	
	
	private List<Object> events;
	private GraphEventBus<T> recordEventBus;
	private GraphEventBus<T> playEventBus;
	//time which should be waited between to outgoing events
	private int sleepTime = 10;
	//the next event to post 
	private int index;

	/**
	 * Creates a new Recroder which is listeneing to the eventbus given as a paramter
	 * @param eventBus
	 * 		The eventbus to which this recorder is listening
	 */
	public RecordPlayer(GraphEventBus<T> eventBus) {
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
