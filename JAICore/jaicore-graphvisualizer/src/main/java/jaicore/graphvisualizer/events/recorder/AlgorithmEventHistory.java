package jaicore.graphvisualizer.events.recorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

public class AlgorithmEventHistory implements ILoggingCustomizable {

	private transient Logger logger = LoggerFactory.getLogger(AlgorithmEventHistory.class);
	private transient String loggerName;

	private List<AlgorithmEventHistoryEntry> entries;

	public AlgorithmEventHistory() {
		this.entries = Collections.synchronizedList(new ArrayList<>());
	}

	public AlgorithmEventHistory(List<AlgorithmEventHistoryEntry> algorithmEventHistoryEntries) {
		this();
		for (AlgorithmEventHistoryEntry entry : algorithmEventHistoryEntries) {
			entries.add(entry);
		}
	}

	public void addEvent(final PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent) {
		AlgorithmEventHistoryEntry entry = this.generateHistoryEntry(propertyProcessedAlgorithmEvent);
		this.entries.add(entry);
		this.logger.debug("Added entry {} for algorithm event {} to history at position {}.", entry, propertyProcessedAlgorithmEvent, this.entries.size() - 1);
	}

	private AlgorithmEventHistoryEntry generateHistoryEntry(final PropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent) {
		return new AlgorithmEventHistoryEntry(propertyProcessedAlgorithmEvent, this.getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		long currentTime = System.currentTimeMillis();
		return currentTime;
	}

	// private long getReceptionTimeOfFirstEvent() {
	// Optional<AlgorithmEventHistoryEntry> firstHistoryEntryOptional = this.entries.stream().findFirst();
	// if (!firstHistoryEntryOptional.isPresent()) {
	// return -1;
	// }
	//
	// return firstHistoryEntryOptional.get().getTimeEventWasReceived();
	// }

	public AlgorithmEventHistoryEntry getEntryAtTimeStep(final int timestep) {
		return this.entries.get(timestep);
	}

	public long getLength() {
		return this.entries.size();
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
