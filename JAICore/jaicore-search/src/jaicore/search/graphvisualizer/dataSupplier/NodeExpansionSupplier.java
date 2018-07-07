package jaicore.search.graphvisualizer.dataSupplier;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.GraphInitializedEvent;
import jaicore.graphvisualizer.events.NodeReachedEvent;
import jaicore.graphvisualizer.events.NodeTypeSwitchEvent;
import jaicore.graphvisualizer.events.VisuEvent;
import jaicore.graphvisualizer.events.controlEvents.ControlEvent;
import jaicore.graphvisualizer.events.controlEvents.NodePushed;
import jaicore.graphvisualizer.gui.dataSupplier.ISupplier;
import jaicore.search.structure.core.Node;

public class NodeExpansionSupplier implements ISupplier {
	
	static int i = 0;
	
	public EventBus eventbus;
	
	private List<VisuEvent> events;
	
	private Node currentRoot;
	
	private int index;
	
	public NodeExpansionSupplier() {
		super();
		this.eventbus = new EventBus();
		events = new ArrayList<VisuEvent>();
		currentRoot = null;
		index = 0;
	}

	@Override
	public void registerListener(Object listener) {
		eventbus.register(listener);
	}

	@Override
	@Subscribe
	public void receiveGraphEvent(VisuEvent event) {
		this.events.add(event);
	}

	@Override
	@Subscribe
	public void receiveControlEvent(ControlEvent event) {
		
		if(event instanceof NodePushed) {
			this.currentRoot = (Node) ((NodePushed) event).getNode();
			System.out.println(((NodePushed) event).getNode());
			forward();
		}
	
					
	}

	@Override
	public JsonNode getSerialization() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 

	private void forward(){
		
		for (int i = 0; i < events.size(); i++) {
			if(eventContains(currentRoot, events.get(i))) {
				if(events.get(i) instanceof NodeReachedEvent) {
					NodeReachedEvent event = (NodeReachedEvent) events.get(i);
					if(event.getNode().equals(currentRoot)) {
						this.eventbus.post(new GraphInitializedEvent(event.getNode()));
						continue;
					}
				}
				this.eventbus.post(events.get(i));
			}
		}
	}
	
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
	
	private boolean eventContains(Node root, VisuEvent event ) {
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
