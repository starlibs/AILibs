package ai.libs.jaicore.graphvisualizer.events.recorder.property;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.graphvisualizer.events.recorder.AlgorithmEventHistoryEntry;

public class PropertyProcessedAlgorithmEventHistory {
	private List<AlgorithmEventHistoryEntry> entries;

	public PropertyProcessedAlgorithmEventHistory() {
		this.entries = new ArrayList<>();
	}

	public PropertyProcessedAlgorithmEventHistory(int size) {
		this.entries = new ArrayList<>(size);
	}

	public PropertyProcessedAlgorithmEventHistory(List<AlgorithmEventHistoryEntry> algorithmEventHistoryEntries) {
		this(algorithmEventHistoryEntries.size());
		for (AlgorithmEventHistoryEntry entry : algorithmEventHistoryEntries) {
			entries.add(entry);
		}
	}

	public List<AlgorithmEventHistoryEntry> getEntries() {
		return entries;
	}

	public AlgorithmEventHistoryEntry getEntryAtTimeStep(final int timestep) {
		return this.entries.get(timestep);
	}

	public long getLength() {
		return this.entries.size();
	}
}
