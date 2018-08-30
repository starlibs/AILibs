package jaicore.graphvisualizer.gui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.IControllableGraphAlgorithm;
import jaicore.graph.IObservableGraphAlgorithm;
import jaicore.graphvisualizer.events.controlEvents.*;
import jaicore.graphvisualizer.events.graphEvents.*;
import jaicore.graphvisualizer.events.misc.AddSupplierEvent;
import jaicore.graphvisualizer.events.misc.AddSupplierEventNew;
import jaicore.graphvisualizer.events.misc.InfoEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.graphvisualizer.gui.dataSupplier.ReconstructionDataSupplier;


import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A recorder class, which is used to record GraphEvents.
 * These graphevents are usually created by a search-algorithm.
 * If is possible to store the recorded events in a file and later load them from one.
 * The recorder is controlled by controll-events.
 *
 * @author jkoepe
 */
public class Recorder {

//    Algorithm to listen to
    private IObservableGraphAlgorithm algorithm;

//    List for storing the events
    private List<Object> receivedEvents;
    private List<Long> receivingTimes;
    private long firstEventTime;

//    Index to know where in the replay the recorder is
    private int index;

//    EventBuses
    private EventBus replayBus;
    private EventBus infoBus;

//    Nodemap to store types of nodes
    private Map<Object, List> nodeMap;

//    List with every datasupplier
    List<ISupplier> supplier;

    /**
     * A constructor for an empty recorder.
     * The empty recorder does not listen to an algorithm but it can load a replay.
     */
    public Recorder(){
        this(null);
    }

    /**
     * Creates a recorder which listens to an algorithm.
     * @param algorithm
     *      The algorithm from which the reocrder receives the events.
     */
    public Recorder(IObservableGraphAlgorithm algorithm){
        if(algorithm != null)
            algorithm.registerListener(this);

        this.algorithm = algorithm;

        //initializing variables

        this.index = 0;

        this.receivedEvents = new ArrayList<>();
        this.receivingTimes = new ArrayList<>();
        this.replayBus = new EventBus();
        this.infoBus = new EventBus();

        this.nodeMap = new HashMap<>();
        supplier = new ArrayList<>();
    }

    /**
     * Register a listener to the replay-Eventbus to receive the graphevents,
     * that are outgoing of the recorder.
     * @param listener
     *      The listener, which is going to receive the graph-Events.
     */
    public void registerReplayListener(Object listener){
        this.replayBus.register(listener);
    }

    /**
     * Register a listener to the info-Eventbus to receive general information
     * of the state of the replay and recorder.
     * Such information are for example the number of received events.
     * @param listener
     *      The listener, which is going to receive the Info-Events.
     *
     */
    public void registerInfoListener(Object listener){
        this.infoBus.register(listener);
    }

    /**
     * This method is used to receive GraphEvents
     * @param event
     */
    @Subscribe
    public void receiveGraphEvent(GraphEvent event){

        boolean updateIndex = false;
        if(this.index == this.receivedEvents.size())
            updateIndex = true;
        //receive event and save the time
        this.receivedEvents.add(event);
        long receiveTime = System.currentTimeMillis();

        //check if it is the first event
        if(firstEventTime == 0)
            firstEventTime = receiveTime;

        //compute the absolute time of the event in relation to the first event
        long eventTime = receiveTime - firstEventTime;
        receivingTimes.add(eventTime);
        this.infoBus.post(new InfoEvent(receivedEvents.size(), eventTime, 0));

    }


    /**
     * Receive a control event and make the appropiate action
     * @param event
     */
    @Subscribe
    public void receiveControlEvent(ControlEvent event){
        if(event instanceof StepEvent){
            if(((StepEvent) event).forward())
                forward(((StepEvent) event).getSteps());
            else
                backward(((StepEvent) event).getSteps());
        }
        if(event instanceof ResetEvent)
            reset();
        if(event instanceof FileEvent){
            if(((FileEvent) event).isLoad())
                this.load(((FileEvent) event).getFile());
            else
                this.save(((FileEvent) event).getFile());
        }
    }


    /**
     * Goes the number of steps forward in the graph. Usually the index + steps do not get higher the number of
     * received Events. The exeption is when there is only one step to do and the index is equal to the number of
     * received Events. In this case the algorithm is triggered.
     *
     * @param steps
     *      The number of steps to do.
     */
    private void forward(int steps){
        if(this.index  == this.receivedEvents.size())
            if(this.algorithm instanceof IControllableGraphAlgorithm) {
                if (this.index == 0)
                    try {
                        ((IControllableGraphAlgorithm) this.algorithm).initGraph();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                else
                    ((IControllableGraphAlgorithm) this.algorithm).step();
            }
        while(steps != 0) {
            if (this.index < this.receivedEvents.size()) {
                Object event = this.receivedEvents.get(index);
                this.replayBus.post(event);

                this.addType(event);
                index ++;
                if(this.index == this.receivedEvents.size())
                    break;
            }

            steps --;
        }
    }


    /**
     * Go backward the number of steps which are given as a paramter
     * @param steps
     * 		The steps to go forward.
     */
    private void backward(int steps){
//        System.out.println(steps);
        if(index == 0)
            return;
        while(index > 0 && steps != 0){
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

    /**
     * Adds the type of a node to the typelist of this node
     * @param event
     *      The event which contains the node
     */
    private void addType(Object event){
        List<String> types;
//            switch the event corresponding to the current event to get the right type of the node
        switch (event.getClass().getSimpleName()) {
            case "GraphInitializedEvent":
                GraphInitializedEvent initializedEvent = (GraphInitializedEvent) event;
                types = new ArrayList();
                types.add("root");
                this.nodeMap.put(initializedEvent.getRoot(), types);
                break;

            case "NodeTypeSwitchEvent":
                NodeTypeSwitchEvent nodeTypeSwitchEvent = (NodeTypeSwitchEvent) event;
                this.nodeMap.get(nodeTypeSwitchEvent.getNode()).add(nodeTypeSwitchEvent.getType());
                break;

            case "NodeReachedEvent":
                NodeReachedEvent nodeReachedEvent = (NodeReachedEvent) event;
                types = new ArrayList<>();
                types.add(nodeReachedEvent.getType());
                this.nodeMap.put(nodeReachedEvent.getNode(), types);
                break;

            default:
                System.out.println("not an allowed event");
                break;
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
     * Adds a new Datasupplier to the recorder. The datasupplier it self is registered at the replaybus to get
     * graphevents from the recorder.
     * @param iSupplier
     */
    public void addDataSupplier(ISupplier iSupplier){
        this.registerReplayListener(iSupplier);
        this.supplier.add(iSupplier);
    }


    /**
     * Posts every known DataSupplier as an addSupplierevent on the InfoBus
     */
    public void getSupplier() {
        for(ISupplier s : supplier)
            this.infoBus.post(new AddSupplierEventNew(s));
    }

    /**
     * Receive a nodePushedEvent and expand the node, does not matter where the search currently is
     */
    @Subscribe
    public void receiveNodePushedEvent(NodePushed event){
        if(this.index == this.receivedEvents.size()) {
            Object node = event.getNode();
            if (this.algorithm instanceof IControllableGraphAlgorithm) {
                try {
                    ((IControllableGraphAlgorithm) this.algorithm).step(node);

                } catch (Exception e){
                    System.out.println("test2");
                }
            }
        }
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

            supplier.stream().forEach(supplier->{
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
                        this.receiveGraphEvent((GraphEvent) event);
                });
            });
//             create the supplier if possible
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
}
