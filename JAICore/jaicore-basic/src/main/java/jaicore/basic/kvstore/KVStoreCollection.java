package jaicore.basic.kvstore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.basic.Maps;
import jaicore.basic.StatisticsUtil;
import jaicore.basic.sets.SetUtil;

public class KVStoreCollection extends LinkedList<KVStore> {

	/** Logger for controlled output. */
	private static final Logger logger = LoggerFactory.getLogger(KVStoreCollection.class);

	/** Automatically generated serial version UID. */
	private static final long serialVersionUID = -4198481782449606136L;

	private static final String LABEL_GROUP_SIZE = "GROUP_SIZE";

	public enum EGroupMethod {
		AVG, MIN, MAX, MAJORITY, MINORITY, LIST, ADD;

		public static EGroupMethod getStandardGroupingHandler() {
			return EGroupMethod.LIST;
		}
	}

	private static final EGroupMethod STANDARD_GROUPING_HANDLER = EGroupMethod.LIST;

	/* META data */
	private static final String FIELD_COLLECTIONID = "collectionID";
	private final KVStore metaData = new KVStore();

	public KVStoreCollection() {

	}

	public KVStoreCollection(final String taskChunkDescription) {
		this.readFrom(taskChunkDescription);
	}

	public KVStoreCollection(final KVStoreCollection other) {
		this.addAll(other);
	}

	public KVStoreCollection(final File file) {
		if (file.isDirectory()) {
			this.setCollectionID(file.getName());
			for (File subFile : file.listFiles()) {
				if (subFile.isFile()) {
					try (BufferedReader br = new BufferedReader(new FileReader(subFile))) {
						String line;
						while ((line = br.readLine()) != null) {
							KVStore kvStore = new KVStore(line);
							kvStore.setCollection(this);
							this.add(kvStore);
						}
					} catch (Exception e) {
						logger.error("An exception occurred while parsing the directory collecting the chunk: {}", e);
					}
				} else {
					try {
						this.readFrom(FileUtil.readFileAsString(file));
					} catch (Exception e) {
						logger.error("An exception occurred while reading the chunk from the given file: {}", e);
					}
				}
			}
		}
	}

	public KVStoreCollection select(final Map<String, String> selection) {
		KVStoreCollection selectedCollection = new KVStoreCollection();
		for (KVStore store : this) {
			if (store.matches(selection)) {
				selectedCollection.add(store);
			}
		}
		return selectedCollection;
	}

	public KVStoreCollection filter(final String[] filterKeys) {
		KVStoreCollection filteredCollection = new KVStoreCollection();
		for (KVStore store : this) {
			store.project(filterKeys);
			filteredCollection.add(store);
		}
		return filteredCollection;
	}

	/** (De-)Serialization handles */
	public void readFrom(final String chunkDescription) {
		String[] lines = chunkDescription.split("\n");
		if (lines.length < 1) {
			throw new IllegalArgumentException("Invalid format of chunk description");
		}

		boolean first = true;
		for (String line : lines) {
			if (line.trim().equals("") || line.trim().startsWith("#")) {
				continue;
			}

			if (first) {
				// first line in chunk description being no white line nor comment
				// such a line must carry all the meta information of the chunk!
				first = false;
				this.metaData.readKVStoreFromDescription(line);
			} else {
				KVStore task = new KVStore(line);
				task.setCollection(this);
				this.add(task);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.metaData.toString() + "\n");
		for (KVStore t : this) {
			sb.append(t.toString() + "\n");
		}
		return sb.toString();
	}

	public void removeAny(final String value) {
		this.removeAny(new String[] { value }, true);
	}

	public void removeAny(final String[] value, final boolean or) {
		List<KVStore> tasksToRemove = new LinkedList<>();
		for (KVStore t : this) {
			if (or) {
				for (String v : value) {
					if (t.toString().contains(v)) {
						tasksToRemove.add(t);
						break;
					}
				}
			} else {
				throw new UnsupportedOperationException("Not yet implemented");
			}
		}
		this.removeAll(tasksToRemove);
	}

	public void removeAny(final Map<String, String> condition, final boolean or) {
		if (or) {
			this.removeIf(t -> {
				for (Entry<String, String> entry : condition.entrySet()) {
					String val = t.getAsString(entry.getKey());
					if (val == null && entry.getValue() == null || val != null && val.equals(entry.getValue())) {
						return true;
					}
				}
				return false;
			});
		} else {
			this.removeIf(t -> {
				for (Entry<String, String> entry : condition.entrySet()) {
					if (!t.getAsString(entry.getKey()).equals(entry.getValue())) {
						return false;
					}
				}
				return true;
			});
		}
	}

	public void removeGroupsIfNotAtLeastWithSize(final int size) {
		Map<String, String> groupSizeCondition = new HashMap<>();
		for (int i = 1; i < size; i++) {
			groupSizeCondition.put(LABEL_GROUP_SIZE, "" + i);
			this.removeAny(groupSizeCondition, true);
		}
	}

	public void removeGroupsIfNotAtLeastWithSizeButOne(final int size, final String[] groupingKeys) {
		Map<String, String> groupSizeCondition = new HashMap<>();
		for (int i = 1; i < size; i++) {
			logger.debug("Remove any groups that dont have at least {} entries.", (i + 1));

			int currentMinLength = i;
			KVStoreCollection group = new KVStoreCollection(this.toString());
			group.renameKey(LABEL_GROUP_SIZE, "size");
			group = group.group(groupingKeys, new HashMap<>());

			for (KVStore t : group) {
				List<Integer> sizeList = t.getAsIntList("size", ",").stream().filter(x -> x > currentMinLength).collect(Collectors.toList());
				logger.debug("{} {} {}", currentMinLength, sizeList, t.getAsIntList("size", ","));
				if (sizeList.size() > 0) {
					for (String groupingKey : groupingKeys) {
						groupSizeCondition.put(groupingKey, t.getAsString(groupingKey));
					}
					groupSizeCondition.put(LABEL_GROUP_SIZE, "" + i);
					logger.debug("{}", groupSizeCondition);
					this.removeAny(groupSizeCondition, false);
				}
			}
		}
	}

	public void renameKey(final String keyName, final String newKeyName) {
		for (KVStore t : this) {
			t.renameKey(keyName, newKeyName);
		}
	}

	public KVStoreCollection group(final String[] groupingKeys, final Map<String, EGroupMethod> groupingHandler) {
		KVStoreCollection tempCollection = new KVStoreCollection();
		tempCollection.setCollectionID(this.getCollectionID());
		Map<String, List<KVStore>> groupedTasks = new HashMap<>();

		for (KVStore t : this) {
			StringBuilder sb = new StringBuilder();
			for (String key : groupingKeys) {
				sb.append(t.getAsString(key) + "#");
			}

			List<KVStore> groupedTaskList = groupedTasks.get(sb.toString());
			if (groupedTaskList == null) {
				groupedTaskList = new LinkedList<>();
				groupedTasks.put(sb.toString(), groupedTaskList);
			}

			groupedTaskList.add(t);
		}

		for (Entry<String, List<KVStore>> groupedTaskEntry : groupedTasks.entrySet()) {
			List<KVStore> groupedTaskList = groupedTaskEntry.getValue();
			KVStore groupedTask = new KVStore(groupedTaskList.get(0));
			groupedTask.put(LABEL_GROUP_SIZE, groupedTaskList.size());

			Map<String, List<Object>> values = new HashMap<>();

			for (KVStore t : groupedTaskList) {
				for (Entry<String, Object> e : t.entrySet()) {
					boolean containedInGrouping = false;
					for (String groupingKey : groupingKeys) {
						if (groupingKey.equals(e.getKey())) {
							containedInGrouping = true;
							break;
						}
					}
					if (containedInGrouping) {
						continue;
					}

					List<Object> objectList = values.get(e.getKey());
					if (objectList == null) {
						objectList = new LinkedList<>();
						values.put(e.getKey(), objectList);
					}
					objectList.add(e.getValue());
				}
			}

			for (Entry<String, List<Object>> valueEntry : values.entrySet()) {
				EGroupMethod groupingMethod = groupingHandler.get(valueEntry.getKey());
				if (groupingMethod == null) {
					groupingMethod = STANDARD_GROUPING_HANDLER;
				}

				Object value = null;
				switch (groupingMethod) {
				case AVG:
					List<Double> valueList = valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList());
					groupedTask.put(valueEntry.getKey() + "_stdDev", StatisticsUtil.standardDeviation(valueList));
					groupedTask.put(valueEntry.getKey() + "_max", StatisticsUtil.max(valueList));
					groupedTask.put(valueEntry.getKey() + "_min", StatisticsUtil.min(valueList));
					groupedTask.put(valueEntry.getKey() + "_var", StatisticsUtil.variance(valueList));
					groupedTask.put(valueEntry.getKey() + "_sum", StatisticsUtil.sum(valueList));
					groupedTask.put(valueEntry.getKey() + "_list", SetUtil.implode(valueList, ","));
					value = StatisticsUtil.mean(valueList);
					break;
				case MIN:
					value = StatisticsUtil.min(valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList()));
					break;
				case MAX:
					value = StatisticsUtil.max(valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList()));
					break;
				case MINORITY:
					value = this.frequentObject(valueEntry.getValue(), false);
					break;
				case MAJORITY:
					value = this.frequentObject(valueEntry.getValue(), true);
					break;
				case ADD:
					value = StatisticsUtil.sum(valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList()));
					break;
				default:
				case LIST:
					value = SetUtil.implode(valueEntry.getValue(), ",");
					break;
				}

				groupedTask.put(valueEntry.getKey(), value);
			}
			tempCollection.add(groupedTask);
		}

		return new KVStoreCollection(tempCollection.toString());
	}

	/**
	 * Searches for the most or least frequent object within a list.
	 *
	 * @param top If set to true most frequent object is returned otherwise the least frequent.
	 * @return The most frequent or least frequent object.
	 */
	private Object frequentObject(final List<Object> listOfObjects, final boolean top) {
		Map<Object, Integer> counterMap = new HashMap<>();
		for (Object v : listOfObjects) {
			Maps.increaseCounterInMap(counterMap, v);
		}

		Object frequentObject = null;
		for (Entry<Object, Integer> counterMapEntry : counterMap.entrySet()) {
			if (frequentObject == null || (top && counterMap.get(counterMapEntry.getKey()) > counterMap.get(frequentObject)) || (!top && counterMap.get(counterMapEntry.getKey()) < counterMap.get(frequentObject))) {
				frequentObject = counterMapEntry.getKey();
			}
		}
		return frequentObject;
	}

	public void merge(final String[] fieldKeys, final String separator, final String newFieldName) {
		for (KVStore t : this) {
			t.merge(fieldKeys, separator, newFieldName);
		}
	}

	public void project(final String[] keepKeys) {
		this.metaData.project(keepKeys);
		for (KVStore t : this) {
			t.project(keepKeys);
		}
	}

	public void projectRemove(final String... removeKeys) {
		this.metaData.removeAll(removeKeys);
		for (KVStore t : this) {
			t.removeAll(removeKeys);
		}
	}

	public void applyFilter(final Map<String, IKVFilter> filterMap) {
		this.metaData.filter(filterMap);
		for (KVStore t : this) {
			t.filter(filterMap);
		}
	}

	public void applyFilter(final String keyName, final IKVFilter filter) {
		Map<String, IKVFilter> filterMap = new HashMap<>();
		filterMap.put(keyName, filter);
		this.applyFilter(filterMap);
	}

	public void mergeTasks(final KVStore other, final Map<String, String> combineMap) {
		for (KVStore t : this) {
			boolean equals = true;
			for (Entry<String, String> combineEntry : combineMap.entrySet()) {
				if (!t.containsKey(combineEntry.getKey()) || !other.containsKey(combineEntry.getValue()) || !t.getAsString(combineEntry.getKey()).equals(other.getAsString(combineEntry.getValue()))) {
					equals = false;
					break;
				}
			}
			if (!equals) {
				continue;
			}
			t.putAll(other);
		}
	}

	public String getCollectionID() {
		return this.metaData.getAsString(FIELD_COLLECTIONID);
	}

	public void setCollectionID(final String collectionID) {
		this.metaData.put(FIELD_COLLECTIONID, collectionID);
	}

	public void serializeTo(final File file) throws IOException {
		this.serializeTo(file, false);
	}

	public void serializeTo(final File file, final boolean append) throws IOException {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, append))) {
			bw.write(this.toString());
		}
	}

	public KVStoreCollection group(final String... groupingKeys) {
		return this.group(groupingKeys, new HashMap<>());
	}

}
