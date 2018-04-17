package jaicore.search.gui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.Subscribe;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeRemovedEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Recorder<T> {


	private List<Object> events;

	private GraphEventBus<T> recordEventBus;
	private GraphEventBus<T> playEventBus;


	//time which should be waited between to outgoing events
	private int sleepTime = 50;
	//the next event to post
	private int index;

	//log the Time of events
	private long firstEvent;
	private List<Long> eventTimes;




	/**
	/**
	 * Creates an empty recorder, which can load an event bus
	 */
	public Recorder(){
		this(null);
	}

	/**
	 * Creates a new Recroder which is listeneing to the eventbus given as a paramter
	 * @param eventBus
	 * 		The eventbus to which this recorder is listening
	 */
	public Recorder(GraphEventBus<T> eventBus) {
		this.index = 0;
		this.recordEventBus = eventBus;
		if(eventBus != null)
			eventBus.register(this);
		playEventBus = new GraphEventBus<>();
		events = new ArrayList<Object>(5);

		firstEvent = 0;
		eventTimes = new ArrayList<>(5);

	}

	/**
	 * receives an event and writes it into a list
	 * @param event
	 */
	@Subscribe
	public void receiveEvent(T event) {
		this.events.add(event);
		long curr = 0;
		if(firstEvent == 0) {
			firstEvent = System.nanoTime();
			curr = firstEvent;
		}
		else
			curr = System.nanoTime();

		this.eventTimes.add(curr-firstEvent);

	}

	/**
	 * Returns the eventbus for the playback
	 * @return
	 * 		The playback eventbus
	 */
	public GraphEventBus<T> getEventBus() {
		return playEventBus;
	}

	/**
	 * posts every event which was stored
	 */
	public void play() {
		while(index < events.size()){
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
		    playEventBus.post(events.get(index));
			index ++;

		}

	}

	/**
	 * posts the next event, while there are events left
	 */
	public void step() {
		//System.out.println(events.get(index).getClass().getSimpleName() + "\t" +index);
		Object event = events.get(index);
		playEventBus.post(event);
		index++;
		//System.out.println(index);
	}


	/**
	 * Resets the index back to 0
	 */
	public void reset(){
		this.index = 0;
	}

	/**
	 * Gets one event backwards.
	 * This is the opposite of step
	 */
	public void back(){
		index--;
		System.out.println(index);
		for(int i = 0; i< index; i ++){
			playEventBus.post(events.get(i));
		}

	}

	/**
	 * Creates an event, which does the opposite of the paramter event
	 * @param object
	 * 		The event, which should be reversed.
	 * @return
	 * 		An event, which can revert the event which was given as a paramter;
	 */
	private Object createCounterEvent(Object object){
		Class eventClass = object.getClass();
		//System.out.println(eventClass.getSimpleName());
		switch(eventClass.getSimpleName()){
			case "GraphInitializedEvent":
				//System.out.println("GraphInitializedEvent");
				return null;


			case "NodeTypeSwitchEvent":
				//NodeTypeSwitchEvent event = (NodeTypeSwitchEvent)object;
				//System.out.println("NodeTypeSwitchEvent");
				return null;



			case "NodeReachedEvent":
				//System.out.println("NodeReachedEvent");
				NodeReachedEvent event = (NodeReachedEvent) object;
				NodeRemovedEvent counter = new NodeRemovedEvent(event.getNode());
				return counter;


			default:
				System.out.println("False");
				break;
		}

		return null;

	}

	public void saveToFile(File file){

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			//events = events.subList(0, 6);
//			Object t = events.get(0);
//			mapper.writeValue(file, t);
			List<LinkedHashMap<Long,Object>> saveList = new ArrayList();
			for(int i = 0; i < events.size(); i++){
				LinkedHashMap t = new LinkedHashMap();
				t.put(eventTimes.get(i), events.get(i));
				saveList.add(t);
			}
//			System.out.println(saveList);

			mapper.writeValue(file, saveList);
//			System.out.println(mapper.writeValueAsString(events));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadFromFile(File file){
		ObjectMapper mapper = new ObjectMapper();
		eventTimes.clear();
		events.clear();
		index = 0 ;

		try {
			List mapList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, LinkedHashMap.class));

			EventCreator creator = new EventCreator();
			mapList.stream().forEach(o ->{
				LinkedHashMap map = (LinkedHashMap) o;
				Set timeSet = map.keySet();
				timeSet.stream().forEach(t->{
					eventTimes.add(Long.parseLong((String)(t)));
					events.add(creator.createEvent((LinkedHashMap)map.get(t)));
				});
			});

			System.out.println(eventTimes);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public int getNumberOfEvents(){
		return events.size();
	}

	public void registerListener(Object listener) {
		this.playEventBus.register(listener);
	}

	public List<Long> getEventTimes() {
		return eventTimes;
	}

	public long getLastEvent(){
		if(! eventTimes.isEmpty())
			return eventTimes.get(eventTimes.size()-1);
		else
			return 0;
	}
}
