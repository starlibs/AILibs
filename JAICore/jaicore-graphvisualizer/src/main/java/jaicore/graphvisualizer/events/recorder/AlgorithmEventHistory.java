package jaicore.graphvisualizer.events.recorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventListener;
import jaicore.graphvisualizer.events.graph.bus.AlgorithmEventSource;

public class AlgorithmEventHistory implements AlgorithmEventSource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AlgorithmEventHistory.class);

	private Set<AlgorithmEventListener> algorithmEventListeners;

	private List<AlgorithmEventHistoryEntry> events;

	public AlgorithmEventHistory() {
		events = Collections.synchronizedList(new ArrayList<>());
		algorithmEventListeners = ConcurrentHashMap.newKeySet();
	}

	public void addEvent(AlgorithmEvent algorithmEvent) {
		events.add(generateHistoryEntry(algorithmEvent));
		for (AlgorithmEventListener listener : algorithmEventListeners) {
			try {
				listener.handleAlgorithmEvent(algorithmEvent);
			} catch (Exception exception) { // TODO change to HandleGraphEventException
				LOGGER.error("Encountered an error when passing graph event to listener \"{}\" .", listener, exception);
			}
		}
	}

	private AlgorithmEventHistoryEntry generateHistoryEntry(AlgorithmEvent algorithmEvent) {
		return new AlgorithmEventHistoryEntry(algorithmEvent, getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		long currentTime = System.currentTimeMillis();
		return Math.min(0, currentTime - getReceptionTimeOfFirstEvent());
	}

	private long getReceptionTimeOfFirstEvent() {
		Optional<AlgorithmEventHistoryEntry> firstHistoryEntryOptional = events.stream().findFirst();
		if (!firstHistoryEntryOptional.isPresent()) {
			return -1;
		}

		return firstHistoryEntryOptional.get().getTimeEventWasReceived();
	}

	public AlgorithmEventHistoryEntry getEntryAtTimeStep(int timestep) {
		return events.get(timestep);
	}

	public long getLength() {
		return events.size();
	}

	@Override
	public void registerListener(AlgorithmEventListener graphEventListener) {
		algorithmEventListeners.add(graphEventListener);
	}

	@Override
	public void unregisterListener(AlgorithmEventListener graphEventListener) {
		algorithmEventListeners.remove(graphEventListener);
	}

}
