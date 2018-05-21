package jaicore.graphvisualizer.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.observation.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.IGraphDataSupplier;
import jaicore.graphvisualizer.IGraphDataVisualizer;
import jaicore.graphvisualizer.TooltipGenerator;
import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeRemovedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;

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
	private Map<Integer, List<String>> toolTipMap;

	private FXController contoller;

	//List with DataSupplier
	private List<IGraphDataSupplier> dataSuppliers;

	private boolean prettyPrint;



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

		this.contoller = null;
		this.dataSuppliers = new ArrayList<>();
		this.prettyPrint = true;

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

		if(contoller != null){
			contoller.updateEventTimes(receivingTimes);
		}

		if(! dataSuppliers.isEmpty())
			for(IGraphDataSupplier supplier : dataSuppliers)
				supplier.receiveEvent(event);
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
		nodeMap.clear();
	}

	public void saveToFile(File file){
		ObjectMapper mapper = new ObjectMapper();
		if(prettyPrint)
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
		try{
			List mapperList = new ArrayList();

			List<LinkedHashMap<Long,Object>> saveList = new ArrayList();

			toolTipMap = new HashMap<>();
			for(int i = 0; i < receivedEvents.size(); i++){
				Object event = receivedEvents.get(i);
				LinkedHashMap<Long, Object> timeToEvent = new LinkedHashMap();

				int code = 0;
				ArrayList<String> tooltips;
				switch (event.getClass().getSimpleName()){
					case "GraphInitializedEvent":
						GraphInitializedEvent graphInitializedEvent = (GraphInitializedEvent) event;
						code = graphInitializedEvent.getRoot().hashCode();
						tooltips = new ArrayList<>();
						tooltips.add(toolTipGenerator.getTooltip(graphInitializedEvent.getRoot()));
						toolTipMap.put(code, tooltips );
						timeToEvent.put(receivingTimes.get(i), new GraphInitializedEvent(code));
						break;

					case "NodeTypeSwitchEvent":
						NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
						code = nodeTypeSwitchEvent.getNode().hashCode();
						toolTipMap.get(code).add(toolTipGenerator.getTooltip(nodeTypeSwitchEvent.getNode()));
						timeToEvent.put(receivingTimes.get(i), new NodeTypeSwitchEvent(code, nodeTypeSwitchEvent.getType()));
						break;

					case "NodeReachedEvent":
						NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
						code = nodeReachedEvent.getNode().hashCode();
						tooltips = new ArrayList<>();
						tooltips.add(toolTipGenerator.getTooltip(nodeReachedEvent.getNode()));
						toolTipMap.put(code, tooltips );
						timeToEvent.put(receivingTimes.get(i), new NodeReachedEvent(nodeReachedEvent.getParent().hashCode(),code, nodeReachedEvent.getType()));
						break;

					default:
						System.out.println("not an allowed event");
						break;
				}
				saveList.add(timeToEvent);
			}


			mapperList.add(saveList);
//			mapperList.add(toolTipMap);
			HashMap<String, JsonNode> supplierHashMap = new HashMap<>();
			for(IGraphDataSupplier s: dataSuppliers)
				supplierHashMap.put(s.getClass().getSimpleName(), s.getSerialization());

			mapperList.add(supplierHashMap);

			mapper.writeValue(file, mapperList);



		} catch (IOException e){
			e.printStackTrace();
		}

	}

	public void loadFromFile(File file){
		//clear existing events
		this.receivedEvents.clear();
		this.receivingTimes.clear();

		this.reset();

		ObjectMapper mapper = new ObjectMapper();

		try {
			List mapperList = mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
//			mapperList.stream().forEach(n->System.out.println(n.getClass()));
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
							event = new GraphInitializedEvent(Integer.parseInt(String.valueOf(eventMap.get("root"))));

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
						receivedEvents.add(event);
				});
			});

			mapperList.stream().filter(o-> mapperList.indexOf(o)!=0).forEach(o->{
				LinkedHashMap map = (LinkedHashMap) o;
				Object[] a = map.keySet().toArray();
				String name = a[0].toString();
				System.out.println(map.get(a[0]));
				HashMap dataMap = (HashMap) map.get(a[0]);
				IGraphDataSupplier supplier = new ReconstructionGraphDataSupplier(dataMap);
				this.contoller.addTab(supplier.getVisualization(),name);
				this.dataSuppliers.add(supplier);

			});

//			toolTipMap = (Map<Integer, List<String>>) mapperList.get(1);
//			this.setTooltipGenerator(node->{
//				List<String> tips = toolTipMap.get(node.toString());
//				int i = nodeMap.get(node).size()-1;
//
//				return tips.get(i);
//			});


		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void registerListener(Object listener) {
		this.replayBus.register(listener);
	}

	public void unregisterListener(Object listener) {

		this.replayBus.unregister(listener);

	}

	public void addDataSupplier(IGraphDataSupplier dataSupplier){
		this.dataSuppliers.add(dataSupplier);
		if(contoller != null)
			this.contoller.addTab(dataSupplier.getVisualization(), dataSupplier.getClass().getSimpleName());

		for(Object event : receivedEvents)
			dataSupplier.receiveEvent(event);


	}

	public void update(Object node){
		for(IGraphDataSupplier dataSupplier : this.dataSuppliers)
			dataSupplier.update(node);
	}

	public void removeDataSupplier(int i) {
		this.dataSuppliers.remove(i);
	}

	public TooltipGenerator getTooltipGenerator() {
		return toolTipGenerator;
	}

	public void setTooltipGenerator(TooltipGenerator toolTipGenerator) {
		this.toolTipGenerator = toolTipGenerator;
	}

	public List<Long> getReceiveTimes() {
		return receivingTimes;
	}

	public void setContoller(FXController ctrl){
		this.contoller = ctrl;
		for(IGraphDataSupplier supplier : dataSuppliers)
			this.contoller.addTab(supplier.getVisualization(), supplier.getClass().getSimpleName());

	}


}
