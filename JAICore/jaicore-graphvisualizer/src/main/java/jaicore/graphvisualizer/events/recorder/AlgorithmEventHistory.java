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

	private Logger logger = LoggerFactory.getLogger(AlgorithmEventHistory.class);
	private String loggerName;

	private List<AlgorithmEventHistoryEntry> events;

	public AlgorithmEventHistory() {
		this.events = Collections.synchronizedList(new ArrayList<>());
	}

	public void addEvent(final AlgorithmEvent algorithmEvent) {
		AlgorithmEventHistoryEntry entry = this.generateHistoryEntry(algorithmEvent);
		this.events.add(entry);
		this.logger.debug("Added entry {} for algorithm event {} to history at position {}.", entry, algorithmEvent, this.events.size() - 1);
	}

	private AlgorithmEventHistoryEntry generateHistoryEntry(final AlgorithmEvent algorithmEvent) {
		return new AlgorithmEventHistoryEntry(algorithmEvent, this.getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		long currentTime = System.currentTimeMillis();
		return Math.max(0, currentTime - this.getReceptionTimeOfFirstEvent());
	}

	private long getReceptionTimeOfFirstEvent() {
		Optional<AlgorithmEventHistoryEntry> firstHistoryEntryOptional = this.events.stream().findFirst();
		if (!firstHistoryEntryOptional.isPresent()) {
			return -1;
		}

		return firstHistoryEntryOptional.get().getTimeEventWasReceived();
	}

	public AlgorithmEventHistoryEntry getEntryAtTimeStep(final int timestep) {
		return this.events.get(timestep);
	}

	public long getLength() {
		return this.events.size();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		this.logger.info("Switching logger name to {}", name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched logger name to {}", name);
	}

}
