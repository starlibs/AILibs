package jaicore.graphvisualizer.events.recorder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.serializable.PropertyProcessedAlgorithmEvent;

public class AlgorithmEventHistory implements ILoggingCustomizable, Serializable {

	private static final long serialVersionUID = 8500970353937357648L;

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
		return System.currentTimeMillis();
	}

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AlgorithmEventHistory other = (AlgorithmEventHistory) obj;
		if (entries == null) {
			if (other.entries != null) {
				return false;
			}
		} else if (!entries.equals(other.entries)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String header = "AlgorithmEventHistory \n";
		StringJoiner joiner = new StringJoiner("\n");
		for (AlgorithmEventHistoryEntry entry : entries) {
			joiner.add(entry.toString());
		}
		return header + joiner.toString();
	}

}
