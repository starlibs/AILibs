package jaicore.basic.chunks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.FileUtil;
import jaicore.basic.ListHelper;
import jaicore.basic.Maps;
import jaicore.basic.StatisticsUtil;
import jaicore.basic.kvstore.IKVFilter;
import jaicore.basic.kvstore.SimpleKVStore;
import jaicore.basic.kvstore.SimpleKVStoreCollection;

public class TaskChunk<V extends Task> extends SimpleKVStore implements Iterable<V>, Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskChunk.class);

	public enum EGroupMethod {
		AVG, MIN, MAX, MAJORITY, MINORITY, LIST, ADD;
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 608124601390225228L;

	private static final EGroupMethod STANDARD_GROUPING_HANDLER = EGroupMethod.LIST;

	private List<V> taskList = new LinkedList<>();

	public TaskChunk(final String taskChunkDescription) {
		this.readFrom(taskChunkDescription);
	}

	public TaskChunk(final TaskChunk<V> other) {
		this.getKeyValueMap().putAll(other.getKeyValueMap());
		this.taskList.addAll(other.taskList);
	}

	public TaskChunk(final File file) {
		if (file.isDirectory()) {
			this.setChunkID(file.getName());
			for (File subFile : file.listFiles()) {
				if (subFile.isFile()) {
					try (BufferedReader br = new BufferedReader(new FileReader(subFile))) {
						String line;
						while ((line = br.readLine()) != null) {
							V task = this.convertLineToTask(line);
							task.setChunk(this);
							this.taskList.add(task);
						}
					} catch (Exception e) {
						LOGGER.error("An exception occurred while parsing the directory collecting the chunk: {}", e);
					}
				} else {
					try {
						this.readFrom(FileUtil.readFileAsString(file));
					} catch (Exception e) {
						LOGGER.error("An exception occurred while reading the chunk from the given file: {}", e);
					}
				}
			}
		}
	}

	public TaskChunk(final Map<String, String> chunkMetaInformation) {
		super(chunkMetaInformation);
		this.taskList = new LinkedList<>();
	}

	public TaskChunk(final Map<String, String> chunkMetaInformation, final List<V> taskList) {
		this(chunkMetaInformation);
		this.taskList.addAll(taskList);
	}

	@Override
	public Iterator<V> iterator() {
		return this.taskList.iterator();
	}

	public synchronized void add(final V task) {
		this.taskList.add(task);
	}

	public synchronized void addAll(final Collection<V> task) {
		this.taskList.addAll(task);
	}

	public synchronized void addAll(final TaskChunk<V> otherChunk) {
		for (V v : otherChunk) {
			this.add(v);
		}
	}

	public Stream<V> stream() {
		return this.taskList.stream();
	}

	public V remove(final int index) {
		return this.taskList.remove(index);
	}

	public boolean remove(final V task) {
		return this.taskList.remove(task);
	}

	public int size() {
		return this.taskList.size();
	}

	public V get(final int index) {
		return this.taskList.get(index);
	}

	public Collection<V> getAll() {
		return this.taskList;
	}

	public void sort(final Comparator<V> comparator) {
		Collections.sort(this.taskList, comparator);
	}

	/** META data */
	private static final String FIELD_CHUNKID = "chunkID";

	public String getChunkID() {
		return this.getValueAsString(FIELD_CHUNKID);
	}

	public void setChunkID(final String chunkID) {
		this.store(FIELD_CHUNKID, chunkID);
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
				this.readKVStoreFromDescription(line);
			} else {
				V task = this.convertLineToTask(line);
				task.setChunk(this);
				this.taskList.add(task);
			}
		}
	}

	@Override
	public String getStringRepresentation() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.getStringRepresentation() + "\n");
		for (Task t : this.taskList) {
			sb.append(t.getStringRepresentation() + "\n");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (Entry<String, String> metaDataEntry : this.getAllKVEntries()) {
			if (first) {
				first = false;
			} else {
				sb.append(";");
			}
			sb.append(metaDataEntry.getKey() + "=" + metaDataEntry.getValue());
		}
		sb.append("\n");

		for (Task task : this.taskList) {
			sb.append(task.toString() + "\n");
		}

		return sb.toString();
	}

	/**
	 * Hook for Override in special instances of TaskChunk.
	 *
	 * @param line
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public V convertLineToTask(final String line) {
		return (V) new Task(line);
	}

	public V getTaskWithID(final String taskID) {
		for (V task : this) {
			if (task.getTaskID().equals(taskID)) {
				return task;
			}
		}
		return null;
	}

	public void removeAny(final String value) {
		this.removeAny(new String[] { value }, true);
	}

	public void removeAny(final String[] value, final boolean or) {
		List<Task> tasksToRemove = new LinkedList<>();
		for (Task t : this.taskList) {
			if (or) {
				for (String v : value) {
					if (t.getStringRepresentation().contains(v)) {
						tasksToRemove.add(t);
						break;
					}
				}
			} else {
				throw new UnsupportedOperationException("Not yet implemented");
			}
		}

		this.taskList.removeAll(tasksToRemove);
	}

	public void removeAny(final Map<String, String> condition, final boolean or) {
		if (or) {
			this.taskList.removeIf(t -> {
				for (String key : condition.keySet()) {
					String val = t.getValueAsString(key);
					if (val == null && condition.get(key) == null || val != null && val.equals(condition.get(key))) {
						return true;
					}
				}
				return false;
			});
		} else {
			this.taskList.removeIf(t -> {
				for (String key : condition.keySet()) {
					if (!t.getValueAsString(key).equals(condition.get(key))) {
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
			groupSizeCondition.put("GROUP_SIZE", "" + i);
			this.removeAny(groupSizeCondition, true);
		}
	}

	public void removeGroupsIfNotAtLeastWithSizeButOne(final int size, final String[] groupingKeys) {

		Map<String, String> groupSizeCondition = new HashMap<>();
		for (int i = 1; i < size; i++) {
			System.out.println("Remove any groups that dont have at least " + (i + 1) + " entries.");

			int currentMinLength = i;
			TaskChunk<V> group = new TaskChunk<>(this.getStringRepresentation());
			group.renameAttribute("GROUP_SIZE", "size");
			group = group.group(groupingKeys, new HashMap<>());

			for (V t : group) {
				List<Integer> sizeList = t.getValueAsIntList("size", ",").stream().filter(x -> x > currentMinLength).collect(Collectors.toList());
				System.out.println(currentMinLength + " " + sizeList + " " + t.getValueAsIntList("size", ","));
				if (sizeList.size() > 0) {
					for (String groupingKey : groupingKeys) {
						groupSizeCondition.put(groupingKey, t.getValueAsString(groupingKey));
					}
					groupSizeCondition.put("GROUP_SIZE", "" + i);
					System.out.println(groupSizeCondition);
					this.removeAny(groupSizeCondition, false);
				}
			}
		}
	}

	public void renameAttribute(final String attributeName, final String replacement) {
		for (V t : this) {
			t.renameAttribute(attributeName, replacement);
		}
	}

	public TaskChunk<V> group(final String[] groupingKeys, final Map<String, EGroupMethod> groupingHandler) {
		TaskChunk<Task> tempChunk = new TaskChunk<>(this.getKeyValueMap());
		tempChunk.setChunkID(this.getChunkID());
		Map<String, List<V>> groupedTasks = new HashMap<>();

		for (V t : this) {
			StringBuilder sb = new StringBuilder();
			for (String key : groupingKeys) {
				sb.append(t.getValueAsString(key) + "#");
			}

			List<V> groupedTaskList = groupedTasks.get(sb.toString());
			if (groupedTaskList == null) {
				groupedTaskList = new LinkedList<>();
				groupedTasks.put(sb.toString(), groupedTaskList);
			}

			groupedTaskList.add(t);
		}

		for (Entry<String, List<V>> groupedTaskEntry : groupedTasks.entrySet()) {
			List<V> groupedTaskList = groupedTaskEntry.getValue();
			Task groupedTask = groupedTaskList.get(0).clone();
			groupedTask.store("GROUP_SIZE", groupedTaskList.size() + "");

			Map<String, List<Object>> values = new HashMap<>();

			for (V t : groupedTaskList) {
				for (Entry<String, String> e : t.getAllKVEntries()) {
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

					Object value;
					try {
						value = Integer.parseInt(e.getValue());

					} catch (Exception noInt) {
						try {
							value = Long.parseLong(e.getValue());
						} catch (Exception noLong) {
							try {
								value = Double.parseDouble(e.getValue());
							} catch (Exception exception2) {
								value = e.getValue();
							}
						}
					}

					List<Object> objectList = values.get(e.getKey());
					if (objectList == null) {
						objectList = new LinkedList<>();
						values.put(e.getKey(), objectList);
					}
					objectList.add(value);
				}
			}

			for (Entry<String, List<Object>> valueEntry : values.entrySet()) {
				EGroupMethod groupingMethod = groupingHandler.get(valueEntry.getKey());
				if (groupingMethod == null) {
					groupingMethod = STANDARD_GROUPING_HANDLER;
				}

				String value = "";
				switch (groupingMethod) {
				case AVG: {
					List<Double> valueList = valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList());
					groupedTask.store(valueEntry.getKey() + "_stdDev", StatisticsUtil.standardDeviation(valueList) + "");
					// groupedTask.store(valueEntry.getKey() + "_max", StatisticsUtil.max(valueList) + "");
					// groupedTask.store(valueEntry.getKey() + "_min", StatisticsUtil.min(valueList) + "");
					// groupedTask.store(valueEntry.getKey() + "_var", StatisticsUtil.variance(valueList) + "");
					// groupedTask.store(valueEntry.getKey() + "_sum", StatisticsUtil.sum(valueList) + "");
					value = StatisticsUtil.mean(valueList) + "";
					break;
				}
				case MIN: {
					List<Double> valueList = valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList());
					value = StatisticsUtil.min(valueList) + "";
					break;
				}
				case MAX: {
					List<Double> valueList = valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList());
					value = StatisticsUtil.max(valueList) + "";
					break;
				}
				case MINORITY: {
					Map<Object, Integer> counterMap = new HashMap<>();
					for (Object v : valueEntry.getValue()) {
						Maps.increaseCounterInMap(counterMap, v);
					}

					Object minorityObject = null;
					for (Object object : counterMap.keySet()) {
						if (minorityObject == null || counterMap.get(object) < counterMap.get(minorityObject)) {
							minorityObject = object;
						}
					}
					value = minorityObject + "";
					break;
				}
				case MAJORITY: {
					Map<Object, Integer> counterMap = new HashMap<>();
					for (Object v : valueEntry.getValue()) {
						Maps.increaseCounterInMap(counterMap, v);
					}

					Object minorityObject = null;
					for (Object object : counterMap.keySet()) {
						if (minorityObject == null || counterMap.get(object) > counterMap.get(minorityObject)) {
							minorityObject = object;
						}
					}
					value = minorityObject + "";
					break;
				}
				case ADD: {
					List<Double> valueList = valueEntry.getValue().stream().map(x -> Double.valueOf(x.toString())).collect(Collectors.toList());
					value = StatisticsUtil.sum(valueList) + "";
					break;
				}
				default:
				case LIST: {
					value = ListHelper.implode(valueEntry.getValue(), ",");
					break;
				}

				}

				groupedTask.store(valueEntry.getKey(), value);
			}
			tempChunk.add(groupedTask);
		}

		return new TaskChunk<>(tempChunk.getStringRepresentation());
	}

	public void implode(final String[] fieldKeys, final String separator, final String newFieldName) {
		for (V t : this) {
			t.implode(fieldKeys, separator, newFieldName);
		}
	}

	@Override
	public void project(final String[] keepKeys) {
		for (V t : this) {
			t.project(keepKeys);
		}
	}

	@Override
	public void projectRemove(final String[] removeKeys) {
		super.projectRemove(removeKeys);

		for (V t : this) {
			t.projectRemove(removeKeys);
		}
	}

	@Override
	public void applyFilter(final Map<String, IKVFilter> filterMap) {
		super.applyFilter(filterMap);
		for (V t : this) {
			t.applyFilter(filterMap);
		}
	}

	public SimpleKVStoreCollection toKVStoreCollection() {
		SimpleKVStoreCollection collection = new SimpleKVStoreCollection();
		collection.addAll(this.taskList);
		return collection;
	}

	public TaskChunk<V> select(final Map<String, String> selection) {
		TaskChunk<V> chunk = new TaskChunk<>(this.getStringRepresentation());
		Set<V> selectedCollection = new HashSet<>();

		for (V t : chunk) {
			if (!t.matches(selection)) {
				selectedCollection.add(t);
			}
		}
		chunk.taskList.removeAll(selectedCollection);
		return chunk;
	}

	public void removeAll(final List<Task> tasksToRemove) {
		this.taskList.removeAll(tasksToRemove);
	}

	public void removeAll(final TaskChunk<V> removeChunk) {
		this.taskList.removeAll(removeChunk.taskList);
	}

	public void mergeTasks(final Task other, final Map<String, String> combineMap) {
		for (Task t : this) {

			boolean equals = true;
			for (Entry<String, String> combineEntry : combineMap.entrySet()) {
				if (!t.containsKey(combineEntry.getKey()) || !other.containsKey(combineEntry.getValue()) || !t.getValueAsString(combineEntry.getKey()).equals(other.getValueAsString(combineEntry.getValue()))) {
					equals = false;
					break;
				}
			}
			if (!equals) {
				continue;
			}
			t.getKeyValueMap().putAll(other.getKeyValueMap());
		}
	}

	public void mannWhitneyU(final String keyFieldName, final String comparatorFieldName, final String valueListFieldName, final String groundTruth) {
		Map<String, Task> groundTruthMap = new HashMap<>();
		Map<String, List<Task>> comparedWithMap = new HashMap<>();

		for (Task t : this) {
			String key = t.getValueAsString(keyFieldName);
			String comparator = t.getValueAsString(comparatorFieldName);

			if (comparator.equals(groundTruth)) {
				groundTruthMap.put(key, t);
			} else {
				List<Task> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}

		for (Entry<String, Task> groundTruthEntry : groundTruthMap.entrySet()) {
			groundTruthEntry.getValue().store("mwu", "");
			List<Task> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> groundTruthList = groundTruthEntry.getValue().getValueAsDoubleList(valueListFieldName, ",");
				double gtMean = StatisticsUtil.mean(groundTruthList);

				for (Task taskToCompareWith : toCompareWithList) {
					List<Double> compareWithValues = taskToCompareWith.getValueAsDoubleList(valueListFieldName, ",");

					boolean sig = StatisticsUtil.mannWhitneyTwoSidedSignificance(groundTruthList, compareWithValues);

					if (groundTruthEntry.getKey().equals("dexter")) {
						System.out.println(groundTruthEntry.getKey());
						System.out.println(groundTruthEntry.getValue());
						// System.out.println(toCompareWithList);
						// System.out.println(toCompareWithList.size());
						System.out.println(taskToCompareWith);
						System.out.println(sig);

						double[] valuesAArray = groundTruthList.stream().mapToDouble(x -> x).toArray();
						double[] valuesBArray = compareWithValues.stream().mapToDouble(x -> x).toArray();
						MannWhitneyUTest test = new MannWhitneyUTest();
						double p = test.mannWhitneyUTest(valuesAArray, valuesBArray);
						System.out.println("p-Value: " + p);
						System.out.println("###");
					}

					if (sig && gtMean < StatisticsUtil.mean(compareWithValues)) {
						taskToCompareWith.store("mwu", "impr");
					} else if (sig && gtMean > StatisticsUtil.mean(compareWithValues)) {
						taskToCompareWith.store("mwu", "deg");
					} else {
						taskToCompareWith.store("mwu", "eq");
					}
				}

			}
		}

	}

	public void best(final String keyFieldName, final String comparatorFieldName, final String valueFieldName) {
		this.best(keyFieldName, comparatorFieldName, valueFieldName, "best");
	}

	public void best(final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final String outputFieldName) {
		Map<String, List<Task>> comparedWithMap = new HashMap<>();

		for (Task t : this) {
			String key = t.getValueAsString(keyFieldName);
			List<Task> taskList = comparedWithMap.get(key);
			if (taskList == null) {
				taskList = new LinkedList<>();
				comparedWithMap.put(key, taskList);
			}
			taskList.add(t);
		}

		for (Entry<String, List<Task>> groundTruthEntry : comparedWithMap.entrySet()) {
			Task bestTask = null;
			for (Task taskToCompareWith : groundTruthEntry.getValue()) {
				if (bestTask == null) {
					bestTask = taskToCompareWith;
					continue;
				}
				double meanBest = StatisticsUtil.mean(bestTask.getValueAsDoubleList(valueFieldName, ","));
				double meanToCompareWith = StatisticsUtil.mean(taskToCompareWith.getValueAsDoubleList(valueFieldName, ","));

				if (meanToCompareWith < meanBest) {
					bestTask = taskToCompareWith;
				}
			}

			for (Task t : comparedWithMap.get(groundTruthEntry.getKey())) {
				if (t.getValueAsString(valueFieldName).equals(bestTask.getValueAsString(valueFieldName))) {
					t.store(outputFieldName, "true");
				} else {
					t.store(outputFieldName, "false");
				}
			}
		}

	}

	public void singleBest(final String keyFieldName, final String comparatorFieldName, final String valueFieldName) {
		this.singleBest(keyFieldName, comparatorFieldName, valueFieldName, "best");
	}

	public void singleBest(final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final String outputFieldName) {
		this.best(keyFieldName, comparatorFieldName, valueFieldName, outputFieldName);
		List<V> distinctTasks = new ArrayList<>();
		Set<String> consideredKeys = new HashSet<>();
		this.taskList.forEach(t -> {
			String keyValue = t.getValueAsString(keyFieldName);
			if (!consideredKeys.contains(keyValue) && t.getValueAsBoolean(outputFieldName)) {
				consideredKeys.add(keyValue);
				distinctTasks.add(t);
			}
		});
		this.taskList = distinctTasks;
	}

	public void best(final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final Set<String> compareObjects) {
		Map<String, List<Task>> comparedWithMap = new HashMap<>();

		for (Task t : this) {
			String key = t.getValueAsString(keyFieldName);
			String object = t.getValueAsString(comparatorFieldName);

			if (!compareObjects.contains(object)) {
				continue;
			}

			List<Task> taskList = comparedWithMap.get(key);
			if (taskList == null) {
				taskList = new LinkedList<>();
				comparedWithMap.put(key, taskList);
			}
			taskList.add(t);
		}

		for (Entry<String, List<Task>> groundTruthEntry : comparedWithMap.entrySet()) {
			Task bestTask = null;
			for (Task taskToCompareWith : groundTruthEntry.getValue()) {
				if (bestTask == null) {
					bestTask = taskToCompareWith;
					continue;
				}
				double meanBest = bestTask.getValueAsDouble(valueFieldName);
				double meanToCompareWith = taskToCompareWith.getValueAsDouble(valueFieldName);

				if (meanToCompareWith < meanBest) {
					bestTask = taskToCompareWith;
				}
			}

			for (Task t : comparedWithMap.get(groundTruthEntry.getKey())) {
				if (t == bestTask) {
					t.store("best", "true");
				} else {
					t.store("best", "false");
				}
			}
		}

	}

	public void bestTTest(final String keyFN, final String idFN, final String valueListFN, final String sigOutputFN) {
		this.best(keyFN, idFN, valueListFN, sigOutputFN + "_best");
		Map<String, Task> groundTruthMap = new HashMap<>();
		Map<String, List<Task>> comparedWithMap = new HashMap<>();

		for (Task t : this) {
			String key = t.getValueAsString(keyFN);
			if (t.getValueAsBoolean(sigOutputFN + "_best")) {
				groundTruthMap.put(key, t);
			} else {
				List<Task> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}
		System.out.println(comparedWithMap);
		for (Entry<String, Task> groundTruthEntry : groundTruthMap.entrySet()) {
			Task gtT = groundTruthEntry.getValue();
			gtT.store(sigOutputFN, "");
			List<Task> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> valueDoubleList = gtT.getValueAsDoubleList(valueListFN, ",");
				double mean1 = StatisticsUtil.mean(valueDoubleList);
				double variance1 = StatisticsUtil.variance(valueDoubleList);
				double max1 = StatisticsUtil.max(valueDoubleList);
				double min1 = StatisticsUtil.min(valueDoubleList);
				double sum1 = StatisticsUtil.sum(valueDoubleList);

				for (Task taskToCompareWith : toCompareWithList) {
					List<Double> valueDoubleList2 = taskToCompareWith.getValueAsDoubleList(valueListFN, ",");
					double mean2 = StatisticsUtil.mean(valueDoubleList2);
					double variance2 = StatisticsUtil.variance(valueDoubleList2);
					double max2 = StatisticsUtil.max(valueDoubleList2);
					double min2 = StatisticsUtil.min(valueDoubleList2);
					double sum2 = StatisticsUtil.sum(valueDoubleList2);

					TTest test = new TTest();
					StatisticalSummaryValues summaryGT = new StatisticalSummaryValues(mean1, variance1, 20, max1, min1, sum1);
					StatisticalSummaryValues summaryComp = new StatisticalSummaryValues(mean2, variance2, 20, max2, min2, sum2);
					boolean sig = test.tTest(summaryGT, summaryComp, 0.05);

					if (sig && mean1 < mean2) {
						taskToCompareWith.store(sigOutputFN, "impr");
					} else if (sig && mean1 > mean2) {
						taskToCompareWith.store(sigOutputFN, "deg");
					} else {
						taskToCompareWith.store(sigOutputFN, "eq");
					}
				}

			}
		}

	}

	public void tTest(final String keyFN, final String idFN, final String valueListFN, final String comparator, final String sigOutputFN) {
		Map<String, Task> groundTruthMap = new HashMap<>();
		Map<String, List<Task>> comparedWithMap = new HashMap<>();

		for (Task t : this) {
			String key = t.getValueAsString(keyFN);
			String compareTo = t.getValueAsString(idFN);

			if (compareTo.equals(comparator)) {
				groundTruthMap.put(key, t);
			} else {
				List<Task> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}

		for (Entry<String, Task> groundTruthEntry : groundTruthMap.entrySet()) {
			Task gtT = groundTruthEntry.getValue();
			gtT.store(sigOutputFN, "");
			List<Task> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> valueList1 = gtT.getValueAsDoubleList(valueListFN, ",");
				double mean1 = StatisticsUtil.mean(valueList1);
				double variance1 = StatisticsUtil.variance(valueList1);
				double max1 = StatisticsUtil.max(valueList1);
				double min1 = StatisticsUtil.min(valueList1);
				double sum1 = StatisticsUtil.sum(valueList1);
				int n1 = valueList1.size();
				String name1 = gtT.getValueAsString(idFN);

				for (Task taskToCompareWith : toCompareWithList) {
					List<Double> valueList2 = taskToCompareWith.getValueAsDoubleList(valueListFN, ",");
					double mean2 = StatisticsUtil.mean(valueList2);
					double variance2 = StatisticsUtil.variance(valueList2);
					int n2 = valueList2.size();
					double max2 = StatisticsUtil.max(valueList2);
					double min2 = StatisticsUtil.min(valueList2);
					double sum2 = StatisticsUtil.sum(valueList2);
					String name2 = taskToCompareWith.getValueAsString(idFN);

					boolean sig;
					TTest test = new TTest();
					StatisticalSummaryValues summaryGT = new StatisticalSummaryValues(mean1, variance1, n1, max1, min1, sum1);
					StatisticalSummaryValues summaryComp = new StatisticalSummaryValues(mean2, variance2, n2, max2, min2, sum2);

					try {
						sig = test.tTest(summaryGT, summaryComp, 0.05);
					} catch (NumberIsTooSmallException e) {
						System.out.println("Cannot apply ttest for dataset " + groundTruthEntry.getKey() + " and comparison of " + name1 + " and " + name2);
						System.out.println(summaryGT);
						System.out.println(summaryComp);
						throw e;
					}

					if (sig && mean1 < mean2) {
						taskToCompareWith.store(sigOutputFN, "impr");
					} else if (sig && mean1 > mean2) {
						taskToCompareWith.store(sigOutputFN, "deg");
					} else {
						taskToCompareWith.store(sigOutputFN, "eq");
					}
				}

			}
		}
	}

	public void serializeTo(final File file) {
		this.serializeTo(file, false);
	}

	public void serializeTo(final File file, final boolean append) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			bw.write(this.getStringRepresentation());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
