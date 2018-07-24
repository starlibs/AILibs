package jaicore.graphvisualizer.events.controlEvents;

public class AlgorithmEvent implements ControlEvent {
	
	private Object node;
	
	public AlgorithmEvent(Object node) {
		this.node = node;
	}

	public Object getNode() {
		return node;
	}

}
