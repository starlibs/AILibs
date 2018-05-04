package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.search.structure.core.AbstractNode;
import jaicore.search.structure.core.GraphEventBus;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.events.GraphInitializedEvent;
import jaicore.search.structure.events.NodeReachedEvent;
import jaicore.search.structure.events.NodeRemovedEvent;
import jaicore.search.structure.events.NodeTypeSwitchEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Recorder<T> {


	private List<Object> events;
	private List<String> nodeTypes;


	private GraphEventBus<T> playEventBus;




	//the next event to post
	private int index;

	//time variables
	private long firstEvent;
	private List<Long> eventTimes;


	private TooltipGenerator tooltipGenerator;

	private Map<Integer, List<ArrayList>> nodeMap;



	/**
	/**
	 * Creates an empty recorder, which can load an event bus
	 */
	public Recorder(){
		this(null);
	}

	/**
	 * Creates a new Recroder which is observing a graph algorithm
	 * @param observable
	 * 		The observable algorithm
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
		nodeTypes = new ArrayList<String>();

		nodeMap = new HashMap<>();


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


		int id = 0;
		Node node = null;

		switch (event.getClass().getSimpleName()){
			case "GraphInitializedEvent":
				nodeTypes.add("root");
				GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
				node= (Node) initializedEvent.getRoot();

				if (node.getPoint() instanceof AbstractNode) {
					AbstractNode abstractNode = (AbstractNode) node.getPoint();
					id = abstractNode.getId();
					List<ArrayList> utility = new ArrayList<>();
					utility.add(new ArrayList<String>());
					utility.add(new ArrayList<String>());
					utility.get(0).add("root");
					utility.get(1).add(tooltipGenerator.getTooltip(node));

					nodeMap.put(id,utility);

				}
				break;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent switchEvent = (NodeTypeSwitchEvent) event;
				String nodeType = switchEvent.getType();
				node = (Node) switchEvent.getNode();
				if(node.getPoint() instanceof AbstractNode){
					AbstractNode abstractNode  = (AbstractNode) node.getPoint();
					id = abstractNode.getId();
					nodeMap.get(id).get(0).add(nodeType);
					nodeMap.get(id).get(1).add(tooltipGenerator.getTooltip(node));
				}


				nodeTypes.add(switchEvent.getType());
				break;

			case "NodeReachedEvent":
				NodeReachedEvent reachedEvent = (NodeReachedEvent) event;
				nodeTypes.add(reachedEvent.getType());

				node= (Node) reachedEvent.getNode();

				if (node.getPoint() instanceof AbstractNode) {
					AbstractNode abstractNode = (AbstractNode) node.getPoint();
					id = abstractNode.getId();
					List<ArrayList> utility = new ArrayList<>();
					utility.add(new ArrayList<String>());
					utility.add(new ArrayList<String>());
					utility.get(0).add(reachedEvent.getType());
					utility.get(1).add(tooltipGenerator.getTooltip(node));

					nodeMap.put(id,utility);

				}

				break;

			default:
				nodeTypes.add("");
				break;
		}


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
		 if(index >= events.size())
					return;
		Object event = events.get(index);



		switch (event.getClass().getSimpleName()){
			case "GraphInitializedEvent":
				nodeTypes.add("root");

				break;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent switchEvent = (NodeTypeSwitchEvent) event;
				String nodeType = switchEvent.getType();
				nodeTypes.add(switchEvent.getType());
				break;

			case "NodeReachedEvent":
				NodeReachedEvent reachedEvent = (NodeReachedEvent) event;
				nodeTypes.add(reachedEvent.getType());
				break;

			default:
				nodeTypes.add("");
				break;
		}

		playEventBus.post(event);
		index++;

	}


	/**
	 * Resets the index back to 0
	 */
	public void reset(){
		this.index = 0;
		nodeTypes.clear();

	}

	/**
	 * Gets one event backwards.
	 * This is the opposite of step
	 */
	public void back(){


		if(index > 0) {
			index--;

 			nodeTypes.remove(nodeTypes.size()-1);
// 			System.out.println(nodeTypes);
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
//				System.out.println(typeSwitchEvent.getType());
				NodeTypeSwitchEvent switchCounter = null;

				Node node = (Node) typeSwitchEvent.getNode();
				if(node.getPoint() instanceof AbstractNode){
					ArrayList typeList = nodeMap.get(((AbstractNode) node.getPoint()).getId()).get(0);

					nodeMap.get(((AbstractNode) node.getPoint()).getId()).get(1).remove(typeList.size()-1);
					typeList.remove(typeList.size()-1);

					String type = (String) typeList.get(typeList.size()-1);
					switchCounter = new NodeTypeSwitchEvent(node,type);
//					System.out.println(type);
				}
				else {
					switch (typeSwitchEvent.getType()) {
						case "or_closed":
							switchCounter = new NodeTypeSwitchEvent(node, "or_expanding");
							break;
						case "or_expanding":
							switchCounter = new NodeTypeSwitchEvent(node, "or_open");
							break;

						default:
							switchCounter = new NodeTypeSwitchEvent(node, nodeTypes.get(nodeTypes.size() - 1));
							break;
					}
				}

//				System.out.println(nodeMap);
				return switchCounter;

			case "NodeReachedEvent":
				NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) object;
				NodeRemovedEvent reachedCounter = new NodeRemovedEvent(nodeReachedEvent.getNode());
				node = (Node) nodeReachedEvent.getNode();
				if(node.getPoint() instanceof AbstractNode){
					nodeMap.remove(((AbstractNode) node.getPoint()).getId());
				}

				return reachedCounter;

			default:
				System.out.println("Not an event");
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

			List mapperList = new ArrayList();
			mapperList.add(saveList);
			mapperList.add(nodeMap);

			mapper.writeValue(file, mapperList);


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
		this.setTooltipGenerator(n->n.toString());

		ObjectMapper mapper =new ObjectMapper();
		try{
			List mapperList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Object.class));

			EventCreator creator = new EventCreator();
			ArrayList eventList = (ArrayList) mapperList.get(0);
			eventList.stream().forEach(o ->{
				//Extract event-times and events from linked-hashmap
				LinkedHashMap map = (LinkedHashMap) o;
				Set timeSet = map.keySet();
				timeSet.stream().forEach(t->{
					eventTimes.add(Long.parseLong((String)(t)));
					events.add(creator.createEvent((LinkedHashMap)map.get(t)));
				});
			});

			HashMap map= (HashMap) mapperList.get(1);
			map.keySet().stream().forEach(n-> {
				Integer id = Integer.parseInt((String) n);
				ArrayList utilityList = (ArrayList) map.get(id.toString());
				this.nodeMap.put(id, utilityList);

			});
		} catch (IOException e){
			e.printStackTrace();
		}

		this.setTooltipGenerator(n -> {
			Node node = (Node) n;
			if ( node.getPoint() instanceof GuiNode){
				int id = ((GuiNode) node.getPoint()).getId();
				List tooltipList = this.nodeMap.get(id).get(1);
				return (String) tooltipList.get(tooltipList.size()-1);
			}
			else
				return node.toString();
		});

//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			//convert JASON-String into a linked list
//			List mapList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, LinkedHashMap.class));
//
//			EventCreator creator = new EventCreator();
//			mapList.stream().forEach(o ->{
//				//Extract event-times and events from linked-hashmap
//				LinkedHashMap map = (LinkedHashMap) o;
//				Set timeSet = map.keySet();
//				timeSet.stream().forEach(t->{
//					eventTimes.add(Long.parseLong((String)(t)));
//					events.add(creator.createEvent((LinkedHashMap)map.get(t)));
//				});
//			});
//		} catch (IOException e) {
//			e.printStackTrace();
//		}


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

	public void setTooltipGenerator(TooltipGenerator<T> tooltipGenerator) {
		this.tooltipGenerator = (TooltipGenerator<T>)tooltipGenerator;
	}

	public TooltipGenerator getTooltipGenerator(){
		return this.tooltipGenerator;
	}
}
