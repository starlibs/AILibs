package jaicore.graphvisualizer.plugin;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.HandleAlgorithmEventException;

public abstract class ASimpleMVCPluginController<M extends IGUIPluginModel, V extends IGUIPluginView> extends Thread implements IGUIPluginController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ASimpleMVCPluginController.class);
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
				try {
					handleAlgorithmEventInternally(event);
				} catch (HandleAlgorithmEventException e) {
					LOGGER.error("An error occurred while handling event {}.", event, e);
				}
			}
		}
	}

	public abstract void handleAlgorithmEventInternally(AlgorithmEvent algorithmEvent) throws HandleAlgorithmEventException;
}
