package jaicore.graphvisualizer.plugin;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public abstract class ASimpleMVCPluginController<M extends IGUIPluginModel, V extends IGUIPluginView> extends Thread implements IGUIPluginController {

	private final Queue<AlgorithmEvent> eventQueue;

	private final V view;
	private final M model;

	public ASimpleMVCPluginController(M model, V view) {
		super();
		this.model = model;
		this.view = view;
		eventQueue = new ConcurrentLinkedQueue<>();
	}

	public M getModel() {
		return model;
	}

	public V getView() {
		return view;
	}

	@Override
	public void handleAlgorithmEvent(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException {
		eventQueue.add(algorithmEvent);
	}

	@Override
	public void run() {
		while (true) {
			AlgorithmEvent event = eventQueue.poll();
			if (event != null) {
				handleAlgorithmEventInternally(event);
			}
		}
	}

	public abstract void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent);
}
