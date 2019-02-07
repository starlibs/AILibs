package jaicore.graphvisualizer.events.recorder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.graphvisualizer.events.graph.GraphEvent;
import jaicore.graphvisualizer.events.graph.bus.GraphEventListener;
import jaicore.graphvisualizer.events.graph.bus.GraphEventSource;

public class GraphEventHistory implements GraphEventSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraphEventHistory.class);

	private Set<GraphEventListener> graphEventListeners;

	private List<GraphEventHistoryEntry> events;

	public GraphEventHistory() {
		events = new ArrayList<>();
		graphEventListeners = ConcurrentHashMap.newKeySet();
	}

	public void addEvent(GraphEvent graphEvent) {
		events.add(generateHistoryEntry(graphEvent));
		for (GraphEventListener listener : graphEventListeners) {
			try {
				listener.handleGraphEvent(graphEvent);
			} catch (Exception exception) { // TODO change to HandleGraphEventException
				LOGGER.error("Encountered an error when passing graph event to listener \"{}\" .", listener, exception);
			}
		}
	}

	private GraphEventHistoryEntry generateHistoryEntry(GraphEvent graphEvent) {
		return new GraphEventHistoryEntry(graphEvent, getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		long currentTime = System.currentTimeMillis();
		return Math.min(0, currentTime - getReceptionTimeOfFirstEvent());
	}

	private long getReceptionTimeOfFirstEvent() {
		Optional<GraphEventHistoryEntry> firstHistoryEntryOptional = events.stream().findFirst();
		if (!firstHistoryEntryOptional.isPresent()) {
			return -1;
		}

		return firstHistoryEntryOptional.get().getTimeEventWasReceived();
	}

	public long getLength() {
		return events.size();
	}

	@Override
	public void registerListener(GraphEventListener graphEventListener) {
		graphEventListeners.add(graphEventListener);
	}

	@Override
	public void unregisterListener(GraphEventListener graphEventListener) {
		graphEventListeners.remove(graphEventListener);
	}

}
