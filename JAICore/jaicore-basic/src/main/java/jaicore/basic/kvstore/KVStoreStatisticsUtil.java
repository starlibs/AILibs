package jaicore.basic.kvstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;

import jaicore.basic.StatisticsUtil;

public class KVStoreStatisticsUtil {

	public static void mannWhitneyU(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueListFieldName, final String groundTruth) {
		Map<String, KVStore> groundTruthMap = new HashMap<>();
		Map<String, List<KVStore>> comparedWithMap = new HashMap<>();

		for (KVStore t : collection) {
			String key = t.getAsString(keyFieldName);
			String comparator = t.getAsString(comparatorFieldName);

			if (comparator.equals(groundTruth)) {
				groundTruthMap.put(key, t);
			} else {
				List<KVStore> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}

		for (Entry<String, KVStore> groundTruthEntry : groundTruthMap.entrySet()) {
			groundTruthEntry.getValue().put("mwu", "");
			List<KVStore> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> groundTruthList = groundTruthEntry.getValue().getAsDoubleList(valueListFieldName, ",");
				double gtMean = StatisticsUtil.mean(groundTruthList);

				for (KVStore taskToCompareWith : toCompareWithList) {
					List<Double> compareWithValues = taskToCompareWith.getAsDoubleList(valueListFieldName, ",");

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
						taskToCompareWith.put("mwu", "impr");
					} else if (sig && gtMean > StatisticsUtil.mean(compareWithValues)) {
						taskToCompareWith.put("mwu", "deg");
					} else {
						taskToCompareWith.put("mwu", "eq");
					}
				}

			}
		}

	}

	public static void best(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueFieldName) {
		best(collection, keyFieldName, comparatorFieldName, valueFieldName, "best");
	}

	public static void best(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final String outputFieldName) {
		Map<String, List<KVStore>> comparedWithMap = new HashMap<>();

		for (KVStore t : collection) {
			String key = t.getAsString(keyFieldName);
			List<KVStore> taskList = comparedWithMap.get(key);
			if (taskList == null) {
				taskList = new LinkedList<>();
				comparedWithMap.put(key, taskList);
			}
			taskList.add(t);
		}

		for (Entry<String, List<KVStore>> groundTruthEntry : comparedWithMap.entrySet()) {
			KVStore bestTask = null;
			for (KVStore taskToCompareWith : groundTruthEntry.getValue()) {
				if (bestTask == null) {
					bestTask = taskToCompareWith;
					continue;
				}
				double meanBest = StatisticsUtil.mean(bestTask.getAsDoubleList(valueFieldName, ","));
				double meanToCompareWith = StatisticsUtil.mean(taskToCompareWith.getAsDoubleList(valueFieldName, ","));

				if (meanToCompareWith < meanBest) {
					bestTask = taskToCompareWith;
				}
			}

			for (KVStore t : comparedWithMap.get(groundTruthEntry.getKey())) {
				if (t.getAsString(valueFieldName).equals(bestTask.getAsString(valueFieldName))) {
					t.put(outputFieldName, "true");
				} else {
					t.put(outputFieldName, "false");
				}
			}
		}
	}

	public static void singleBest(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueFieldName) {
		singleBest(collection, keyFieldName, comparatorFieldName, valueFieldName, "best");
	}

	public static void singleBest(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final String outputFieldName) {
		best(collection, keyFieldName, comparatorFieldName, valueFieldName, outputFieldName);
		List<KVStore> distinctTasks = new ArrayList<>();
		Set<String> consideredKeys = new HashSet<>();
		collection.forEach(t -> {
			String keyValue = t.getAsString(keyFieldName);
			if (!consideredKeys.contains(keyValue) && t.getAsBoolean(outputFieldName)) {
				consideredKeys.add(keyValue);
				distinctTasks.add(t);
			}
		});
		collection.clear();
		collection.addAll(distinctTasks);
	}

	public static void best(final KVStoreCollection collection, final String keyFieldName, final String comparatorFieldName, final String valueFieldName, final Set<String> compareObjects) {
		Map<String, List<KVStore>> comparedWithMap = new HashMap<>();

		for (KVStore t : collection) {
			String key = t.getAsString(keyFieldName);
			String object = t.getAsString(comparatorFieldName);

			if (!compareObjects.contains(object)) {
				continue;
			}

			List<KVStore> taskList = comparedWithMap.get(key);
			if (taskList == null) {
				taskList = new LinkedList<>();
				comparedWithMap.put(key, taskList);
			}
			taskList.add(t);
		}

		for (Entry<String, List<KVStore>> groundTruthEntry : comparedWithMap.entrySet()) {
			KVStore bestTask = null;
			for (KVStore taskToCompareWith : groundTruthEntry.getValue()) {
				if (bestTask == null) {
					bestTask = taskToCompareWith;
					continue;
				}
				double meanBest = bestTask.getAsDouble(valueFieldName);
				double meanToCompareWith = taskToCompareWith.getAsDouble(valueFieldName);

				if (meanToCompareWith < meanBest) {
					bestTask = taskToCompareWith;
				}
			}

			for (KVStore t : comparedWithMap.get(groundTruthEntry.getKey())) {
				if (t == bestTask) {
					t.put("best", true);
				} else {
					t.put("best", false);
				}
			}
		}

	}

	public static void bestTTest(final KVStoreCollection collection, final String keyFN, final String idFN, final String valueListFN, final String sigOutputFN) {
		best(collection, keyFN, idFN, valueListFN, sigOutputFN + "_best");
		Map<String, KVStore> groundTruthMap = new HashMap<>();
		Map<String, List<KVStore>> comparedWithMap = new HashMap<>();

		for (KVStore t : collection) {
			String key = t.getAsString(keyFN);
			if (t.getAsBoolean(sigOutputFN + "_best")) {
				groundTruthMap.put(key, t);
			} else {
				List<KVStore> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}
		for (Entry<String, KVStore> groundTruthEntry : groundTruthMap.entrySet()) {
			KVStore gtT = groundTruthEntry.getValue();
			gtT.put(sigOutputFN, "");
			List<KVStore> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> valueDoubleList = gtT.getAsDoubleList(valueListFN, ",");
				double mean1 = StatisticsUtil.mean(valueDoubleList);
				double variance1 = StatisticsUtil.variance(valueDoubleList);
				double max1 = StatisticsUtil.max(valueDoubleList);
				double min1 = StatisticsUtil.min(valueDoubleList);
				double sum1 = StatisticsUtil.sum(valueDoubleList);

				for (KVStore taskToCompareWith : toCompareWithList) {
					List<Double> valueDoubleList2 = taskToCompareWith.getAsDoubleList(valueListFN, ",");
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
						taskToCompareWith.put(sigOutputFN, "impr");
					} else if (sig && mean1 > mean2) {
						taskToCompareWith.put(sigOutputFN, "deg");
					} else {
						taskToCompareWith.put(sigOutputFN, "eq");
					}
				}

			}
		}

	}

	public static void tTest(final KVStoreCollection collection, final String keyFN, final String idFN, final String valueListFN, final String comparator, final String sigOutputFN) {
		Map<String, KVStore> groundTruthMap = new HashMap<>();
		Map<String, List<KVStore>> comparedWithMap = new HashMap<>();

		for (KVStore t : collection) {
			String key = t.getAsString(keyFN);
			String compareTo = t.getAsString(idFN);

			if (compareTo.equals(comparator)) {
				groundTruthMap.put(key, t);
			} else {
				List<KVStore> taskList = comparedWithMap.get(key);
				if (taskList == null) {
					taskList = new LinkedList<>();
					comparedWithMap.put(key, taskList);
				}
				taskList.add(t);
			}
		}

		for (Entry<String, KVStore> groundTruthEntry : groundTruthMap.entrySet()) {
			KVStore gtT = groundTruthEntry.getValue();
			gtT.put(sigOutputFN, "");
			List<KVStore> toCompareWithList = comparedWithMap.get(groundTruthEntry.getKey());

			if (toCompareWithList != null) {
				List<Double> valueList1 = gtT.getAsDoubleList(valueListFN, ",");
				double mean1 = StatisticsUtil.mean(valueList1);
				double variance1 = StatisticsUtil.variance(valueList1);
				double max1 = StatisticsUtil.max(valueList1);
				double min1 = StatisticsUtil.min(valueList1);
				double sum1 = StatisticsUtil.sum(valueList1);
				int n1 = valueList1.size();
				String name1 = gtT.getAsString(idFN);

				for (KVStore taskToCompareWith : toCompareWithList) {
					List<Double> valueList2 = taskToCompareWith.getAsDoubleList(valueListFN, ",");
					double mean2 = StatisticsUtil.mean(valueList2);
					double variance2 = StatisticsUtil.variance(valueList2);
					int n2 = valueList2.size();
					double max2 = StatisticsUtil.max(valueList2);
					double min2 = StatisticsUtil.min(valueList2);
					double sum2 = StatisticsUtil.sum(valueList2);
					String name2 = taskToCompareWith.getAsString(idFN);

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
						taskToCompareWith.put(sigOutputFN, "impr");
					} else if (sig && mean1 > mean2) {
						taskToCompareWith.put(sigOutputFN, "deg");
					} else {
						taskToCompareWith.put(sigOutputFN, "eq");
					}
				}

			}
		}
	}

}
