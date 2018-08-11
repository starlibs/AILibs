package jaicore.graphvisualizer.guiOld;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeRemovedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.misc.AddSupplierEvent;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import jaicore.graphvisualizer.events.misc.RequestSuppliersEvent;
import jaicore.graphvisualizer.guiOld.dataSupplier.ISupplier;
import jaicore.graphvisualizer.guiOld.dataSupplier.ReconstructionDataSupplier;


/**
 * A recorder class, which is used to record events created by a search-algorithm. 
 * It is possible to store these events in a file or load them out of a file.
 * Furthermore these class listens to controllEvents and acts accordingly.
 * 
 * @author jkoepe
 *
 * @param <T>
 */
public class Recorder<T> implements IObservableGraphAlgorithm {

	//List of datasuppliers
	private List<ISupplier> suppliers;

	//Algorithm
	private IObservableGraphAlgorithm algorithm;

	//List for storing the events and the receiving times
	private List<Object> receivedEvents;
	private List<Long> receivingTimes;
	private long firstEventTime;


	//Index to know where in the replay the recorder is
	private int index;

	private boolean live;

	//EventBuses
	private EventBus replayBus;
	private EventBus infoBus;

	//NodeMap to store types of Nodes
	private Map<Object, List> nodeMap;


	/**
	 * A constructor for an empty recorder.
	 */
	public Recorder(){
		this(null);
	}

	/**
	 * Creates a recorder which listens to an algorithm.
	 * @param algorithm
	 * 		The algorith to which the recorder should listen.
	 */
	public Recorder(IObservableGraphAlgorithm algorithm){
		if(algorithm != null)
			algorithm.registerListener(this);

		this.algorithm = algorithm;


		this.index = 0;
		this.suppliers = new ArrayList<>();

		this.receivedEvents = new ArrayList();
		this.receivingTimes = new ArrayList();
		this.replayBus = new EventBus();
		this.infoBus = new EventBus();

		this.nodeMap = new HashMap<>();
		this.live = false;

	}

	@Override
	public void registerListener(Object listener) {
		this.replayBus.register(listener);
	}

	/**
	 * Registers a listener, which wants to get the infoevents which are posted on an different eventbus then the graphevents.
	 * @param listener
	 */
	public void registerInfoListener(Object listener){
		this.infoBus.register(listener);
		if(!receivedEvents.isEmpty())
			this.infoBus.post(new InfoEvent(receivedEvents.size()-1, receivingTimes.get(receivingTimes.size()-1), this.suppliers.size() ));
	}

	@Subscribe
	public void receiveVisuEvent(GraphEvent event){
		//receive event and save the time
		this.receivedEvents.add(event);
		long receiveTime = System.currentTimeMillis();

		//check if it is the first event
		if(firstEventTime == 0)
			firstEventTime = receiveTime;

		//compute the absolute time of the event in relation to the first event
		long eventTime = receiveTime - firstEventTime;
		receivingTimes.add(eventTime);

		//post a new infoevent to update the listener.
		this.infoBus.post(new InfoEvent(receivedEvents.size(), eventTime , this.suppliers.size()));

		//if the livemodus is enabled post every event and set the index on the maxindex
		if(live){
			this.replayBus.post(event);
			this.index = receivedEvents.size()-1;
		}
	}

	@Subscribe
	public void receiveControlEvent(ControlEvent event){
		String eventName = event.getClass().getSimpleName();

		switch(eventName){
//		 StepEvents are only processed if the modus is not in live mode, 
//		since in live mode the events are posts directly by receiving the events
			case "StepEvent":
				if(!live) {
					StepEvent stepEvent = (StepEvent) event;
					if (stepEvent.forward())
						forward(stepEvent.getSteps());
					else
						backward(stepEvent.getSteps());
				}
				break;

//				receive an fileEvent and determine if it is for loading or storing
			case "FileEvent":
				FileEvent fileEvent = (FileEvent) event;
				if(fileEvent.isLoad())
					load(fileEvent.getFile());
				else
					save(fileEvent.getFile());
				break;

//				receives an resetevent and resets the recorder
			case "ResetEvent":
				reset();
				break;

//				receive an isliveevent and set the live state accordingly
			case "IsLiveEvent":
				IsLiveEvent islive = (IsLiveEvent)event;
				this.live= islive.isLive();
				break;
			default:
//				System.out.println(eventName);
				break;
		}
	}


	/**
	 * Go forward the number of steps which are given as a paramter
	 * @param steps
	 * 		The steps to go forward.
	 */
	private void forward(int steps){
		//run as long there are steps
		while(index < receivedEvents.size() && steps != 0) {
			this.replayBus.post(receivedEvents.get(index));
			Object event = receivedEvents.get(index);

			List<String> types;
			//switch the event corresponding to the current events and post them to the replaybus
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
			steps --;
			index ++;
		}

	}

	/**
	 * Go backward the number of steps which are given as a paramter
	 * @param steps
	 * 		The steps to go forward.
	 */
	private void backward(int steps){
		if(index == 0)
			return;
		while(index > 0 && steps != 0) {
			index --;
			this.replayBus.post(counterEvent(receivedEvents.get(index)));
			steps --;

		}
	}

	/**
	 * Creates a counterevent to the event which was given.
	 * Currently the events which can be countered are most of the graphevents 
	 * @param event
	 * 		The event to which a counter event should be created
	 * @return
	 * 		The counter event
	 */
	public Object counterEvent(Object event) {
		Object counter = null;


		switch (event.getClass().getSimpleName()) {
//			counter for a GraphInitializedEvent
			case "GraphInitializedEvent":
				//just for completion
				counter = null;
				break;

//				counter for a nodetypeswitchevent
			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
				List<String> typeList = nodeMap.get(nodeTypeSwitchEvent.getNode());
				typeList.remove(typeList.size() - 1);
				counter = new NodeTypeSwitchEvent(nodeTypeSwitchEvent.getNode(), typeList.get(typeList.size() - 1));
				break;

//				counter for a nodereached event
			case "NodeReachedEvent":
				NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
				counter = new NodeRemovedEvent(nodeReachedEvent.getNode());
				break;

			default:
				System.out.println("not an allowed event");
				break;
		}
		return counter;
	}

	/**Saves the Events in a file
	 *
	 * @param file
	 * 		The file to which the events are stored.
	 */
	private void save(File file){
		ObjectMapper mapper = new ObjectMapper();

		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try{
			List mapperList = new ArrayList();

			List<LinkedHashMap<Long,Object>> saveList = new ArrayList();

			for(int i = 0; i < receivedEvents.size(); i++){
				Object event = receivedEvents.get(i);
				LinkedHashMap<Long, Object> timeToEvent = new LinkedHashMap();
				int code = 0;

				//Maps times to the hashcodes of the events
				switch (event.getClass().getSimpleName()){
					case "GraphInitializedEvent":
						GraphInitializedEvent graphInitializedEvent = (GraphInitializedEvent) event;
						code = graphInitializedEvent.getRoot().hashCode();
						timeToEvent.put(receivingTimes.get(i), new GraphInitializedEvent(code));
						break;

					case "NodeTypeSwitchEvent":
						NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
						code = nodeTypeSwitchEvent.getNode().hashCode();

						timeToEvent.put(receivingTimes.get(i), new NodeTypeSwitchEvent(code, nodeTypeSwitchEvent.getType()));
						break;

					case "NodeReachedEvent":
						NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
						code = nodeReachedEvent.getNode().hashCode();
						timeToEvent.put(receivingTimes.get(i), new NodeReachedEvent(nodeReachedEvent.getParent().hashCode(),code, nodeReachedEvent.getType()));
						break;

					default:
						System.out.println("not an allowed event");
						break;
				}
				saveList.add(timeToEvent);
			}
//			add the serialized supplier to a list which gets saved
			mapperList.add(saveList);
			HashSet<JsonNode> supplierHashSet = new HashSet<>();

			suppliers.stream().forEach(supplier->{
				supplierHashSet.add(supplier.getSerialization());
			});

			mapperList.add(supplierHashSet);

			mapper.writeValue(file, mapperList);



		} catch (IOException e){
			e.printStackTrace();
		}


	}

	/**
	 * Load events from a file
	 * @param file
	 */
	private void load(File file) {

		//clear existing events
		this.receivedEvents.clear();
		this.receivingTimes.clear();

		this.reset();

		ObjectMapper mapper = new ObjectMapper();

		try {
			List mapperList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
			ArrayList eventList = (ArrayList) mapperList.get(0);
//			create the events out of the stored ones. In the newly loaded events the hashcode of the nodes of the old ones are the whole node
			eventList.stream().forEach(n->{
				LinkedHashMap map = (LinkedHashMap) n;
				map.keySet().stream().forEach(time->receivingTimes.add(Long.parseLong((String) time)));
				map.values().stream().forEach(v->{
					LinkedHashMap eventMap = (LinkedHashMap) v;

					int node;
					Object event;
					switch(eventMap.get("name").toString()){
						case "GraphInitializedEvent":
							int hashCode= (int) eventMap.get("root");
							event = new GraphInitializedEvent(Integer.parseInt(String.valueOf(eventMap.get("root"))));
							System.out.println(hashCode);
							break;

						case "NodeTypeSwitchEvent":
							node = Integer.parseInt(String.valueOf(eventMap.get("node")));
							event = new NodeTypeSwitchEvent(node, eventMap.get("type").toString());
							break;

						case "NodeReachedEvent":
							int parent = Integer.parseInt(String.valueOf(eventMap.get("parent")));
							node = Integer.parseInt(String.valueOf(eventMap.get("node")));
							event = new NodeReachedEvent(parent, node, eventMap.get("type").toString());
							break;

						default:
							event = null;


					}
					if(event != null)
						this.receiveVisuEvent((GraphEvent) event);
				});
			});
			// create the supplier if possible
			mapperList.stream().filter(o-> mapperList.indexOf(o)!=0).forEach(o->{
				ArrayList m = (ArrayList) o;
				LinkedHashMap map = (LinkedHashMap) m.get(0);
				ReconstructionDataSupplier supplier = new ReconstructionDataSupplier(map);
				this.addDataSupplier(supplier);
			});


		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Resets the recorder.
	 * To do this only the current nodemap and the index a clear or set to 0.
	 */
	private void reset() {
		this.index = 0;
		nodeMap.clear();
	}

	/**
	 * Adds a datasupplier to the recorder
	 * @param supplier
	 */
	public void addDataSupplier(ISupplier supplier){
		this.suppliers.add(supplier);
		if(algorithm != null)
			algorithm.registerListener(supplier);
		this.infoBus.post(new AddSupplierEvent(supplier));
	}

	/**
	 * Receive a requestSupplierEvent and  send out the current suppliers
	 * @param event
	 */
	@Subscribe
	public void receiveRequestSupplierEvent(RequestSuppliersEvent event){
		for (ISupplier supplier : this.suppliers){
			this.infoBus.post(new AddSupplierEvent(supplier));
		}
	}

	/**
	 * Returns the algorithm
	 * @return
	 */
	public IObservableGraphAlgorithm getAlgorithm() {
		return algorithm;
	}
}
