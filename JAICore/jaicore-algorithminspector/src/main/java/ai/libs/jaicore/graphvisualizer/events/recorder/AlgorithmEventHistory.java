package ai.libs.jaicore.graphvisualizer.events.recorder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import org.api4.java.algorithm.IAlgorithm;
import org.api4.java.algorithm.events.serializable.IPropertyProcessedAlgorithmEvent;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AlgorithmEventHistory} stores {@link AlgorithmEventHistoryEntry}s constructed from {@link IPropertyProcessedAlgorithmEvent}s representing the recorded behavior of an {@link IAlgorithm}. Such an {@link AlgorithmEventHistory} can
 * be stored and loaded using an
 * {@link AlgorithmEventHistorySerializer}.
 *
 * @author atornede
 *
 */
public class AlgorithmEventHistory implements ILoggingCustomizable, Serializable {

	private static final long serialVersionUID = 8500970353937357648L;

	private transient Logger logger = LoggerFactory.getLogger(AlgorithmEventHistory.class);
	private transient String loggerName;

	private List<AlgorithmEventHistoryEntry> entries;

	/**
	 * Creates a new {@link AlgorithmEventHistory}.
	 */
	public AlgorithmEventHistory() {
		this.entries = Collections.synchronizedList(new ArrayList<>());
	}

	/**
	 * Creates a new {@link AlgorithmEventHistory} with the given {@link List} of {@link AlgorithmEventHistoryEntry}s.
	 *
	 * @param algorithmEventHistoryEntries The list of {@link AlgorithmEventHistoryEntry}s to be stored in the history.
	 */
	public AlgorithmEventHistory(final List<AlgorithmEventHistoryEntry> algorithmEventHistoryEntries) {
		this();
		for (AlgorithmEventHistoryEntry entry : algorithmEventHistoryEntries) {
			this.entries.add(entry);
		}
	}

	/**
	 * Adds the given {@link IPropertyProcessedAlgorithmEvent} to this {@link AlgorithmEventHistoryEntry}.
	 *
	 * @param propertyProcessedAlgorithmEvent The {@link IPropertyProcessedAlgorithmEvent} to be added to this history.
	 */
	public void addEvent(final IPropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent) {
		AlgorithmEventHistoryEntry entry = this.generateHistoryEntry(propertyProcessedAlgorithmEvent);
		this.entries.add(entry);
		this.logger.debug("Added entry {} for algorithm event {} to history at position {}.", entry, propertyProcessedAlgorithmEvent, this.entries.size() - 1);
	}

	private AlgorithmEventHistoryEntry generateHistoryEntry(final IPropertyProcessedAlgorithmEvent propertyProcessedAlgorithmEvent) {
		return new AlgorithmEventHistoryEntry(propertyProcessedAlgorithmEvent, this.getCurrentReceptionTime());
	}

	private long getCurrentReceptionTime() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns the {@link AlgorithmEventHistoryEntry} at the given timestep.
	 *
	 * @param timestep The timestep for which the {@link AlgorithmEventHistoryEntry} has to be returned.
	 * @return The {@link AlgorithmEventHistoryEntry} at the given timestep.
	 */
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
		result = prime * result + ((this.entries == null) ? 0 : this.entries.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		AlgorithmEventHistory other = (AlgorithmEventHistory) obj;
		if (this.entries == null) {
			if (other.entries != null) {
				return false;
			}
		} else if (!this.entries.equals(other.entries)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String header = "AlgorithmEventHistory \n";
		StringJoiner joiner = new StringJoiner("\n");
		for (AlgorithmEventHistoryEntry entry : this.entries) {
			joiner.add(entry.toString());
		}
		return header + joiner.toString();
	}

}
