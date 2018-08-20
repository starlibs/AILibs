package jaicore.basic.chunks;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jaicore.basic.kvstore.SimpleKVStore;

public class Task extends SimpleKVStore {
	/**
	 *
	 */
	private static final long serialVersionUID = -8259795114344707332L;

	private static final String FIELD_TASKID = "taskID";
	private TaskChunk<? extends Task> chunk;

	public Task() {
		super();
	}

	public Task(final String stringRepresentation) {
		super(stringRepresentation);
	}

	public Task(final Task taskToCopy) {
		super(new HashMap<>());
		this.chunk = taskToCopy.chunk;
		for (Entry<String, String> entry : taskToCopy.getAllKVEntries()) {
			this.store(entry.getKey(), entry.getValue());
		}
	}

	public Task(final Map<String, String> keyValueMap) {
		super(keyValueMap);
	}

	@Override
	public Task clone() {
		Task t = new Task();
		t.getKeyValueMap().putAll(this.getKeyValueMap());
		return t;
	}

	public void setChunk(final TaskChunk<? extends Task> chunk) {
		this.chunk = chunk;
	}

	public TaskChunk<? extends Task> getChunk() {
		return this.chunk;
	}

	public String getTaskID() {
		return this.getValueAsString(FIELD_TASKID);
	}

	public void setTaskID(final String taskID) {
		this.store(FIELD_TASKID, taskID);
	}

	public Collection<Task> storeEach(final String key, final Set<String> values) {
		return Task.storeEach(this, key, values);
	}

	public static Collection<Task> storeEach(final Task task, final String key, final Set<String> values) {
		Collection<Task> allCombinations = new HashSet<>();
		for (String value : values) {
			Task copy = new Task(task);
			copy.store(key, value);
			allCombinations.add(copy);
		}
		return allCombinations;
	}

	@Override
	public String toString() {
		return super.toString();
	}

	public void implode(final String[] fieldKeys, final String separator, final String newKey) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String fieldKey : fieldKeys) {
			if (first) {
				first = false;
			} else {
				sb.append(separator);
			}
			sb.append(this.getValueAsString(fieldKey));
			this.getKeyValueMap().remove(fieldKey);
		}
		this.store(newKey, sb.toString());
	}

	public void prefixKeys(final String prefix) {
		Set<String> keySet = new HashSet<>(this.getKeyValueMap().keySet());
		for (String key : keySet) {
			String value = this.getValueAsString(key);
			this.getKeyValueMap().remove(key);
			this.store(prefix + key, value);
		}
	}

	public void renameAttribute(final String attributeName, final String replacement) {
		if (this.containsKey(attributeName)) {
			this.store(replacement, this.getValueAsString(attributeName));
			this.projectRemove(new String[] { attributeName });
		}
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Task)) {
			return false;
		}

		Task t = (Task) other;
		return t.getKeyValueMap().equals(this.getKeyValueMap());
	}

}
