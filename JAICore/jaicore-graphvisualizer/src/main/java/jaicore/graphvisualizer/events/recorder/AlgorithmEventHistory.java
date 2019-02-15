package jaicore.graphvisualizer.events.recorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;

public class AlgorithmEventHistory implements ILoggingCustomizable {

	private String loggerName;
	private Logger logger = LoggerFactory.getLogger(AlgorithmEventHistory.class);

	private List<AlgorithmEventHistoryEntry> events;

	public AlgorithmEventHistory() {
		events = Collections.synchronizedList(new ArrayList<>());
	}

	public void addEvent(AlgorithmEvent algorithmEvent) {
		AlgorithmEventHistoryEntry entry = generateHistoryEntry(algorithmEvent);
		events.add(entry);
		logger.debug("Added entry {} for algorithm event {} to history at position {}.", entry, algorithmEvent, events.size() - 1);
	}

	private AlgorithmEventHistoryEntry generateHistoryEntry(AlgorithmEvent algorithmEvent) {
		return new AlgorithmEventHistoryEntry(algorithmEvent, getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		long currentTime = System.currentTimeMillis();
		return Math.max(0, currentTime - getReceptionTimeOfFirstEvent());
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
	public String getLoggerName() {
		return loggerName;
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}

}
