package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.SearchVisualizationPanel;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeRemovedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Recorder<T> implements IObservableGraphAlgorithm {


	/* List which contains every received event */
	private List<Object> receivedEvents;

	//index for iterating over the events
	private int index;

	//receiving times
	private List<Long> receivingTimes;
	private long firstEventTime;


	//EventBus for Replays;
	private EventBus replayBus;
	private TooltipGenerator toolTipGenerator;

	private Map<Object, List> nodeMap;
	private Map<Object, String> toolTipMap;


	/**
	 * Creator for an empty recorder
	 */
	public Recorder(){
		this(null);
	}

	/**
	 * Creates a recorder which is listening to an IObservableGraphAlgorithm
	 * @param algorithm
	 * 		The algorithm to which this recorder is listening.
	 */
	public Recorder(IObservableGraphAlgorithm algorithm){
		if(algorithm != null)
		algorithm.registerListener(this);

		//initial assignments
		this.receivedEvents = new ArrayList<>();
		this.receivingTimes = new ArrayList<>();
		this.index = 0;
		this.firstEventTime = 0;
		this.replayBus = new EventBus();
		this.nodeMap = new HashMap<>();
	}

	/**
	 * receive an event on a event bus
	 * @param event
	 * 		The event which was received.
	 */
	@Subscribe
	public void receiveEvent(T event){
		this.receivedEvents.add(event);
		long receiveTime = System.currentTimeMillis();

		if(firstEventTime == 0)
			firstEventTime = receiveTime;

		receivingTimes.add(receiveTime-firstEventTime);
	}

	/**
	 * one step forward
	 */
	public void step(){
		if(index == this.receivedEvents.size())
			return;

		this.replayBus.post(this.receivedEvents.get(this.index));

		Object event = receivedEvents.get(index);
		List<String> types;

		switch(event.getClass().getSimpleName()){
			case "GraphInitializedEvent":
				GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
				types = new ArrayList();
				types.add("root");
				nodeMap.put(initializedEvent.getRoot(), types);
				break;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
				nodeMap.get(nodeTypeSwitchEvent.getNode()).add(nodeTypeSwitchEvent.getType());
				break;

			case "NodeReachedEvent":
				NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
				types = new ArrayList<>();
				types.add(nodeReachedEvent.getType());
				nodeMap.put(nodeReachedEvent.getNode(),types);
				break;

			default:
				System.out.println("not an allowed event");
				break;
		}

		this.index++;
	}


	private void play(){
	}

	/**
	 * one step backwards
	 */
	public void back(){
		this.index --;

		Object counter = null;
		Object event = this.receivedEvents.get(index);

		switch (event.getClass().getSimpleName()){
			case "GraphInitializedEvent":
				//just for completion
				counter = null;
				break;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
				List<String> typeList = nodeMap.get(nodeTypeSwitchEvent.getNode());
				typeList.remove(typeList.size()-1);
				counter = new NodeTypeSwitchEvent(nodeTypeSwitchEvent.getNode(), typeList.get(typeList.size()-1));
				break;

			case "NodeReachedEvent":
				NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
				counter = new NodeRemovedEvent(nodeReachedEvent.getNode());
				break;

			default:
				System.out.println("not an allowed event");
				break;

		}

		replayBus.post(counter);

	}

	public void reset(){
		index = 0;
	}

	public void saveToFile(File file){
		ObjectMapper mapper = new ObjectMapper();
		try{
			List mapperList = new ArrayList();

			List<LinkedHashMap<Long,Object>> saveList = new ArrayList();

			//create a HashMap, which contains the event as values and the corresponding time as a value
			//This is done to store the time in the same file as the events.
			for(int i = 0; i < receivedEvents.size(); i++){
				LinkedHashMap t = new LinkedHashMap();
				t.put(receivingTimes.get(i), receivedEvents.get(i));
				saveList.add(t);
			}

			for (Object node: nodeMap.keySet()){
				toolTipMap.put(node, toolTipGenerator.getTooltip(node));
			}



			mapperList.add(saveList);
			mapperList.add(toolTipMap);


			mapper.writeValue(file, mapperList);

		} catch (IOException e){
			e.printStackTrace();
		}

	}

	public void loadFromFile(File file){}

	public void registerListener(Object listener) {
		this.replayBus.register(listener);
	}

	public void unregisterListener(Object listener) {
		this.replayBus.unregister(listener);
	}



	public TooltipGenerator getToolTipGenerator() {
		return toolTipGenerator;
	}

	public void setToolTipGenerator(TooltipGenerator toolTipGenerator) {
		this.toolTipGenerator = toolTipGenerator;
	}

	public List<Long> getReceiveTimes() {
		return receivingTimes;
	}
}
