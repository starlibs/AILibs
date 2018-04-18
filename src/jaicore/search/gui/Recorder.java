package jaicore.search.gui;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeRemovedEvent;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Recorder<T> {


	private List<Object> events;

	private GraphEventBus<T> recordEventBus;
	private GraphEventBus<T> playEventBus;




	//the next event to post
	private int index;

	//time variables
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
	public Recorder(IObservableGraphAlgorithm observable) {
		this.index = 0;
		//this.recordEventBus = eventBus;
		if(observable != null)
			observable.registerListener(this);
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
		if(index > 0) {
			index--;
//			System.out.println(events.get(index).getClass());
			Object counter = createCounterEvent(events.get(index));
			playEventBus.post(counter);
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
		switch(eventClass.getSimpleName()){
			case "GraphInitializedEvent":
				//just for completion, the controller handles the back button for the first event as an reset
				return null;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent typeSwitchEvent = (NodeTypeSwitchEvent)object;
				System.out.println(typeSwitchEvent.getType());
				return null;

			case "NodeReachedEvent":
				NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) object;
				NodeRemovedEvent counter = new NodeRemovedEvent(nodeReachedEvent.getNode());
				System.out.println("nodeReachedEvent");
				return counter;

			default:
				System.out.println("False");
				break;
		}

		return null;

	}

	/**
	 * Stores the received events in the specified file.
	 * @param file
	 * 		The file in which the events should be stored
	 */
	public void saveToFile(File file){

		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			List<LinkedHashMap<Long,Object>> saveList = new ArrayList();

			//create a HashMap, which contains the event as values and the corresponding time as a value
			//This is done to store the time in the same file as the events.
			for(int i = 0; i < events.size(); i++){
				LinkedHashMap t = new LinkedHashMap();
				t.put(eventTimes.get(i), events.get(i));
				saveList.add(t);
			}


			mapper.writeValue(file, saveList);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Load events from the specified file.
	 * @param file
	 * 		The file which contains the stored events.
	 */
	public void loadFromFile(File file){

		//reset the recorder to not mix up the old events with the loaded ones
		eventTimes.clear();
		events.clear();
		index = 0 ;

		ObjectMapper mapper = new ObjectMapper();
		try {
			//convert JASON-String into a linked list
			List mapList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, LinkedHashMap.class));

			EventCreator creator = new EventCreator();
			mapList.stream().forEach(o ->{
				//Extract event-times and events from linked-hashmap
				LinkedHashMap map = (LinkedHashMap) o;
				Set timeSet = map.keySet();
				timeSet.stream().forEach(t->{
					eventTimes.add(Long.parseLong((String)(t)));
					events.add(creator.createEvent((LinkedHashMap)map.get(t)));
				});
			});
		} catch (IOException e) {
			e.printStackTrace();
		}


	}



	/**
	 * Register a new listener to the replayEventBus
	 * @param listener
	 * 		The listener to be registerd to the replayEventBus.
	 */
	public void registerListener(Object listener) {
		this.playEventBus.register(listener);
	}

	public void unregisterListener(Object listener){
		this.playEventBus.unregister(listener);
	}

	/**
	 *
	 * @return
	 * 		The number of received events.
	 */
	public int getNumberOfEvents(){
		return events.size();
	}

	/**
	 *
	 * @return
	 */
	public List<Long> getEventTimes() {
		return eventTimes;
	}


	/**
	 * Returns the Time, at which the last event was recieved in relation to the first received event.
	 * @return
	 * 		Returns the time at which the last event was received in nano seconds.
	 */
	public long getLastEvent(){
		if(! eventTimes.isEmpty())
			return eventTimes.get(eventTimes.size()-1);
		else
			return 0;
	}
}
