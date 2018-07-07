package jaicore.graphvisualizer.gui;

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

import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeRemovedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.VisuEvent;
import jaicore.graphvisualizer.events.add.AddSupplierEvent;
import jaicore.graphvisualizer.events.add.InfoEvent;
import jaicore.graphvisualizer.events.add.RequestSuppliersEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataSupplier.ReconstructionDataSupplier;


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

	//EventBuses
	private EventBus replayBus;
	private EventBus infoBus;

	//NodeMap to store types of Nodes
	private Map<Object, List> nodeMap;


	public Recorder(){
		this(null);
	}

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

	}

	@Override
	public void registerListener(Object listener) {
		this.replayBus.register(listener);
	}

	public void registerInfoListener(Object listener){
		this.infoBus.register(listener);
		if(!receivedEvents.isEmpty())
			this.infoBus.post(new InfoEvent(receivedEvents.size()-1, receivingTimes.get(receivingTimes.size()-1), this.suppliers.size() ));
	}

	@Subscribe
	public void receiveVisuEvent(VisuEvent event){
		this.receivedEvents.add(event);
		long receiveTime = System.currentTimeMillis();

		if(firstEventTime == 0)
			firstEventTime = receiveTime;

		long eventTime = receiveTime - firstEventTime;
		receivingTimes.add(eventTime);

		this.infoBus.post(new InfoEvent(receivedEvents.size(), eventTime , this.suppliers.size()));
//		if(contoller != null){
////			contoller.updateEventTimes(receivingTimes);
//			contoller.updateTimeLine();
//		}
//
//		if(! dataSuppliers.isEmpty())
//			for(INodeDataSupplier supplier : dataSuppliers)
//				supplier.receiveEvent(event);
	}

	@Subscribe
	public void receiveControlEvent(ControlEvent event){
		String eventName = event.getClass().getSimpleName();

		switch(eventName){
			case "StepEvent":
				StepEvent stepEvent = (StepEvent) event;
				if(stepEvent.forward())
					forward(stepEvent.getSteps());
				else
					backward(stepEvent.getSteps());
				break;

			case "FileEvent":
				FileEvent fileEvent = (FileEvent) event;
				if(fileEvent.isLoad())
					load(fileEvent.getFile());
				else
					save(fileEvent.getFile());
				break;

			case "ResetEvent":
				reset();
				break;
			default:
//				System.out.println(eventName);
				break;
		}
	}



	private void forward(int steps){
		while(index < receivedEvents.size() && steps != 0) {
			this.replayBus.post(receivedEvents.get(index));
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
			steps --;
			index ++;
		}

	}

	private void backward(int steps){
		if(index == 0)
			return;
		while(index > 0 && steps != 0) {
			index --;
			this.replayBus.post(counterEvent(receivedEvents.get(index)));
			steps --;

		}
	}

	public Object counterEvent(Object event) {
		Object counter = null;


		switch (event.getClass().getSimpleName()) {
			case "GraphInitializedEvent":
				//just for completion
				counter = null;
				break;

			case "NodeTypeSwitchEvent":
				NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
				List<String> typeList = nodeMap.get(nodeTypeSwitchEvent.getNode());
				typeList.remove(typeList.size() - 1);
				counter = new NodeTypeSwitchEvent(nodeTypeSwitchEvent.getNode(), typeList.get(typeList.size() - 1));
				break;

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

				//Maps times to events
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

	private void load(File file) {

		//clear existing events
		this.receivedEvents.clear();
		this.receivingTimes.clear();

		this.reset();

		ObjectMapper mapper = new ObjectMapper();

		try {
			List mapperList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Object.class));

			ArrayList eventList = (ArrayList) mapperList.get(0);
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
						this.receiveVisuEvent((VisuEvent) event);
				});
			});

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

	private void reset() {
		this.index = 0;
		nodeMap.clear();
	}

	public void addDataSupplier(ISupplier supplier){
		this.suppliers.add(supplier);
		if(algorithm != null)
			algorithm.registerListener(supplier);
		this.infoBus.post(new AddSupplierEvent(supplier));
	}

	@Subscribe
	public void receiveRequestSupplierEvent(RequestSuppliersEvent event){
		for (ISupplier supplier : this.suppliers){
			this.infoBus.post(new AddSupplierEvent(supplier));
		}
	}
}
