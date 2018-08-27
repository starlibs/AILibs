package jaicore.search.gui.dataSupplier;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.IsLiveEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.events.controlEvents.ResetEvent;
import jaicore.graphvisualizer.events.controlEvents.StepEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphEvent;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.search.structure.core.Node;

/**
 * A class which works similar to the recorder.
 * In contrast to the recorder the sent graphsEvents are filtered by the branch.
 * @author jkoepe
 *
 */
public class NodeExpansionSupplier implements ISupplier {
	
	// variabled
	static int i = 0;
	
	public EventBus eventbus;
	
	private List<GraphEvent> events;
	
	private Node currentRoot;
	
	private int index;

	private int currentIndex;

	
	/**
	 * Creates a new NodeExpansionSupplier
	 */
	public NodeExpansionSupplier() {
		super();
		this.eventbus = new EventBus();
		events = new ArrayList<GraphEvent>();
		currentRoot = null;
		index = 0;

	}

	
	@Override
	public void registerListener(Object listener) {
		eventbus.register(listener);
	}

	@Override
	@Subscribe
	public void receiveGraphEvent(GraphEvent event) {
		this.events.add(event);
	}

	@Override
	@Subscribe
	public void receiveControlEvent(ControlEvent event) {
		
		// if a node was pushed, the branch out of this node is the last pushed node
		if(event instanceof NodePushed) {
			this.currentRoot = (Node) ((NodePushed) event).getNode();
			System.out.println(((NodePushed) event).getNode());
			show(0);
		}



		if(event instanceof StepEvent)
			if(((StepEvent) event).forward()) {
				index += ((StepEvent) event).getSteps();
				if(currentRoot != null)
					show(index-((StepEvent) event).getSteps());
			}
		if (event instanceof ResetEvent) {
			this.index = 0;
		}
	}

	@Override
	public JsonNode getSerialization() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 
/**
 * shows the branch and post every event till the current index given by the controller
 * @param start
 */
	private void show(int start){
		int i = start;
//		for (int i = start; i < index; i++) {
        while(i < index && i < events.size()){
            if (eventContains(currentRoot, events.get(i))) {
                if (events.get(i) instanceof NodeReachedEvent) {
                    NodeReachedEvent event = (NodeReachedEvent) events.get(i);
                    if (event.getNode().equals(currentRoot)) {
                        this.eventbus.post(new GraphInitializedEvent(event.getNode()));
                        continue;
                    }
                }
                this.eventbus.post(events.get(i));
            }
            i++;
        }
	}
	
	/**
	 * Check if the branch contains a node
	 * @param root
	 * @param node
	 * @return
	 */
	private boolean contains(Node root, Node node) {
		if(root.equals(node))
			return true;
		else {
			if(node.getParent()== null)
				return false;
			else
				return contains(root, node.getParent());
		}
	}
	
	/**
	 * checks if the node is contained in an event
	 * @param root
	 * @param event
	 * @return
	 */
	private boolean eventContains(Node root, GraphEvent event ) {
		if(event instanceof GraphInitializedEvent) {
			if(((GraphInitializedEvent) event).getRoot() instanceof Node)
				return contains(root, (Node) ((GraphInitializedEvent) event).getRoot());
			
		}
		
		if(event instanceof NodeReachedEvent) {
			if(((NodeReachedEvent) event).getNode() instanceof Node) {
				return contains(root, (Node) ((NodeReachedEvent) event).getNode());
			}
			
		}
		if(event instanceof NodeTypeSwitchEvent) {
			if(((NodeTypeSwitchEvent) event).getNode() instanceof Node)
				return contains(root, (Node) ((NodeTypeSwitchEvent) event).getNode());
		}
		
		return false;
	}

}
