package jaicore.graphvisualizer.gui;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jaicore.graph.IControllableGraphAlgorithm;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.FileEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.misc.InfoEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private IControllableGraphAlgorithm algorithm;

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
    public Recorder(IControllableGraphAlgorithm algorithm){
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



        if(updateIndex) {
            this.replayBus.post(event);
            this.addType(event);
            this.index = receivedEvents.size();
            //post a new infoevent to update the listener.
            this.infoBus.post(new InfoEvent(receivedEvents.size(), eventTime,0, true));
        }
        else
            this.infoBus.post(new InfoEvent(receivedEvents.size(), eventTime, 0));

    }


    @Subscribe
    public void receiveControlEvent(ControlEvent event){
        if(event instanceof StepEvent){
            if(((StepEvent) event).forward())
                forward(((StepEvent) event).getSteps());
            else
                backward(((StepEvent) event).getSteps());
        }
    }


    private void forward(int steps){
        while(steps != 0) {
            if (this.index < this.receivedEvents.size()) {
                Object event = this.receivedEvents.get(index);
                this.replayBus.post(event);

                this.addType(event);
                index ++;

            } else if (this.index == this.receivedEvents.size()) {
                if (this.index == 0)
                    try {
                        this.algorithm.initGraph();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                else
                    this.algorithm.step();
            }

            steps --;
        }




//        System.out.println(steps);
//        if (this.index == this.receivedEvents.size()){
//            if(index == 0) {
//                try {
//                    this.algorithm.initGraph();
//                } catch (Throwable throwable) {
//                    throwable.printStackTrace();
//                }
//            }
//            else
//                this.algorithm.step();
//        }

    }

    private void backward(int steps){
        System.out.println(steps);
    }

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


}
