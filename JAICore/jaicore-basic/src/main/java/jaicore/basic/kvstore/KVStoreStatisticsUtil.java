package jaicore.basic.kvstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.StatisticsUtil;

/**
 * This util may be used to compute some statistics and carrying out significance tests.
 * In particular implementations for three different significance tests are provided:
 *
 * t-test - requirements: data distribution must follow a normal distribution and it must be sampled independently from the two populations.
 * Wilcoxon signed-rank test - requirements: sample variables d_i = x_i,1 - x_i,2 have to be iid and symmetric.
 * MannWhitneyU - requirements: all observations from both groups are independent of each other, responses are (at least) ordinal, i.e. one can say which one is better.
 *
 * @author mwever
 */
public class KVStoreStatisticsUtil {

	/* logging */
	private static final Logger logger = LoggerFactory.getLogger(KVStoreStatisticsUtil.class);

	/* default key name of the best annotation */
	private static final String DEFAULT_OUTPUT_BEST = "best";
	private static final String DEFAULT_OUTPUT_RANK = "rank";

	private KVStoreStatisticsUtil() {
		/* Private c'tor to prevent instanstantiation of this class. */
	}

	/**
	 * For each setting this method finds the best mean value for setting <code>setting</code> among all the <code>sampleIDs</code> averaging the <code>sampledValues</code> (minimization).
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 */
	public static void best(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues) {
		best(collection, setting, sampleID, sampledValues, DEFAULT_OUTPUT_BEST);
	}

	public static void rank(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues) {
		rank(collection, setting, sampleID, sampledValues, DEFAULT_OUTPUT_RANK);
	}

	public static void rank(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output) {
		rank(collection, setting, sampleID, sampledValues, output, true);
	}

	public static void rank(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output, final boolean minimize) {
		KVStoreCollection grouped = new KVStoreCollection(collection);
		grouped.group(setting, sampleID);

		TwoLayerKVStoreCollectionPartition partition = new TwoLayerKVStoreCollectionPartition(setting, sampleID, grouped);

		for (Entry<String, Map<String, KVStoreCollection>> partitionEntry : partition) {
			List<KVStore> competitorList = new LinkedList<>();
			partitionEntry.getValue().values().stream().map(x -> x.get(0)).forEach(competitorList::add);
			Collections.sort(competitorList, (o1, o2) -> (minimize) ? Double.compare(StatisticsUtil.mean(o1.getAsDoubleList(sampledValues)), StatisticsUtil.mean(o2.getAsDoubleList(sampledValues)))
					: Double.compare(StatisticsUtil.mean(o2.getAsDoubleList(sampledValues)), StatisticsUtil.mean(o1.getAsDoubleList(sampledValues))));

			for (int i = 0; i < competitorList.size(); i++) {
				competitorList.get(i).put(output, (i + 1));
			}

		}

	}

	/**
	 * For each setting this method finds the best mean value for setting <code>setting</code> among all the <code>sampleIDs</code> averaging the <code>sampledValues</code> (minimization).
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param output The name of the field where to store the result to.
	 * @param minimize Whether minimum is better or not.
	 */
	public static void best(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output, final boolean minimize) {
		Set<String> availableSampleIDs = collection.stream().map(x -> x.getAsString(sampleID)).collect(Collectors.toSet());
		best(collection, setting, sampleID, sampledValues, availableSampleIDs, output, minimize);
	}

	/**
	 * For each setting this method finds the best mean value for setting <code>setting</code> among all the <code>sampleIDs</code> averaging the <code>sampledValues</code> (minimization).
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param output The name of the field where to store the result to.
	 */
	public static void best(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output) {
		best(collection, setting, sampleID, sampledValues, output, true);
	}

	/**
	 * For each setting this method finds the best mean value for setting <code>setting</code> among all the <code>sampleIDs</code> averaging the <code>sampledValues</code> (minimization).
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param sampleIDsToConsider The set of sample IDs which are to be considered in the comparison.
	 * @param output The name of the field where to store the result to.
	 * @param minimize Whether minimum is better or not.
	 */
	public static void best(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final Set<String> sampleIDsToConsider, final String output, final boolean minimize) {
		KVStoreCollection grouped = new KVStoreCollection(collection);
		grouped.group(setting, sampleID);

		KVStoreCollectionPartition partition = new KVStoreCollectionPartition(setting, collection);

		for (Entry<String, KVStoreCollection> entry : partition) {

			OptionalDouble bestValue;
			if (minimize) {
				bestValue = entry.getValue().stream().filter(x -> sampleIDsToConsider.contains(x.getAsString(sampleID)))
						.mapToDouble(x -> (x.get(sampledValues) != null) ? StatisticsUtil.mean(x.getAsDoubleList(sampledValues)) : Double.MAX_VALUE).min();
			} else {
				bestValue = entry.getValue().stream().filter(x -> sampleIDsToConsider.contains(x.getAsString(sampleID)))
						.mapToDouble(x -> (x.get(sampledValues) != null) ? StatisticsUtil.mean(x.getAsDoubleList(sampledValues)) : Double.MIN_VALUE).max();
			}

			if (bestValue.isPresent()) {
				double best = bestValue.getAsDouble();
				for (KVStore store : entry.getValue()) {

					if (store.get(sampledValues) != null) {
						store.put(output, StatisticsUtil.mean(store.getAsDoubleList(sampledValues)) == best);
					} else {
						Double surrogateValue = Double.MIN_VALUE;
						if (minimize) {
							surrogateValue = Double.MAX_VALUE;
						}
						store.put(output, surrogateValue == best);
					}

				}
			}
		}
	}

	/**
	 * Computes a 1-to-n Wilcoxon signed rank test to compare a single sample to each other sample of the collection.
	 * For the significance test a pair-wise signed rank test is used to test the hypothesis whether the two considered
	 * related samples stem from the same distribution (H_0).
	 *
	 * @param collection The collection of KVStores to carry out the wilcoxon signed rank test for.
	 * @param setting The field name of the setting description for each of which the wilcoxon has to be computed, e.g. dataset.
	 * @param sampleIDs The field name of the identifier of what is to be compared, e.g. the different approaches.
	 * @param pairingIndex The field name of the index according to which samples are internally paired, e.g. seed for the random object.
	 * @param nam)eOfTestPopulation The value of the targetOfComparison field that is to be used as the 1 sample which is compared to the n other samples.
	 * @param output The field name where to put the results of the significance tests.
	 */
	public static KVStoreCollection wilcoxonSignedRankTest(final KVStoreCollection collection, final String setting, final String sampleIDs, final String pairingIndex, final String sampledValues, final String nameOfTestPopulation,
			final String output) {
		KVStoreCollection groupedCollection = new KVStoreCollection(collection);
		groupedCollection.group(setting, sampleIDs);

		TwoLayerKVStoreCollectionPartition settingAndSampleWisePartition = new TwoLayerKVStoreCollectionPartition(setting, sampleIDs, groupedCollection);

		for (Entry<String, Map<String, KVStoreCollection>> settingToSampleWisePartition : settingAndSampleWisePartition) {
			/* Description of the ground truth. */
			KVStore onesStore = settingToSampleWisePartition.getValue().get(nameOfTestPopulation).get(0);
			if (onesStore == null) {
				continue;
			}

			onesStore.put(output, ESignificanceTestResult.TIE);
			Map<String, Double> sampleMapOfOne = toSampleMap(onesStore.getAsStringList(pairingIndex, ","), onesStore.getAsDoubleList(sampledValues, ","));

			for (Entry<String, KVStoreCollection> sampleData : settingToSampleWisePartition.getValue().entrySet()) {
				if (sampleData.getKey().equals(nameOfTestPopulation)) {
					continue;
				}

				KVStore otherStore = sampleData.getValue().get(0);

				Map<String, Double> sampleMapOfOther = toSampleMap(otherStore.getAsStringList(pairingIndex, ","), otherStore.getAsDoubleList(sampledValues, ","));

				Set<String> mergedSampleIDs = new HashSet<>(sampleMapOfOne.keySet());
				mergedSampleIDs.addAll(sampleMapOfOther.keySet());

				double[] one = new double[mergedSampleIDs.size()];
				double[] other = new double[mergedSampleIDs.size()];

				double meanOne = 0.0;
				double meanOther = 0.0;

				int counter = 0;
				for (String sampleID : mergedSampleIDs) {
					if (sampleMapOfOne.containsKey(sampleID)) {
						one[counter] = sampleMapOfOne.get(sampleID);
						meanOne += one[counter] / sampleMapOfOne.size();
					} else {
						one[counter] = Double.NaN;
					}

					if (sampleMapOfOther.containsKey(sampleID)) {
						other[counter] = sampleMapOfOther.get(sampleID);
						meanOther += other[counter] / sampleMapOfOther.size();
					} else {
						other[counter] = Double.NaN;
					}
					counter++;
				}

				if (StatisticsUtil.wilcoxonSignedRankSumTestTwoSided(one, other)) {
					if (meanOne < meanOther) {
						otherStore.put(output, ESignificanceTestResult.INFERIOR);
					} else {
						otherStore.put(output, ESignificanceTestResult.SUPERIOR);
					}
				} else {
					otherStore.put(output, ESignificanceTestResult.TIE);
				}
			}
		}
		return groupedCollection;
	}

	/**
	 * Computes a (pair-wise) 1-to-n MannWhitneyU statistic to compare a single sample from one population to each other sample of the other populations.
	 * For the significance test the MannWhitneyU test statistic is used to test the hypothesis whether the two considered
	 * related samples stem from the same distribution (H_0). As a result the tests KVStores have a sig-test result value in the output field. The output
	 * is to be interpreted as how the other population compares to the test population, if the value is superior the other population is significantly
	 * better than the tested population and vice versa.
	 *
	 * @param collection The collection of KVStores to carry out the MannWhitneyU test for.
	 * @param setting The field name of the setting description for each of which the MannWhitneyU has to be computed, e.g. dataset.
	 * @param sampleIDs The field name of the identifier of what is to be compared, e.g. the different approaches.
	 * @param pairingIndex The field name of the index according to which samples are internally paired, e.g. seed for the random object.
	 * @param nameOfTestPopulation The value of the targetOfComparison field that is to be used as the 1 sample which is compared to the n other samples.
	 * @param output The field name where to put the results of the significance tests.
	 */
	public static void mannWhitneyU(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String nameOfTestPopulation, final String output) {
		for (Entry<String, Map<String, KVStoreCollection>> settingWiseEntry : prepareGroupedTwoLayerPartition(collection, setting, sampleID)) {
			KVStoreCollection testPopulation = settingWiseEntry.getValue().get(nameOfTestPopulation);
			if (testPopulation == null || testPopulation.isEmpty()) {
				continue;
			}

			testPopulation.get(0).put(output, ESignificanceTestResult.TIE);
			List<Double> testValues = testPopulation.get(0).getAsDoubleList(sampledValues);

			for (Entry<String, KVStoreCollection> otherEntry : settingWiseEntry.getValue().entrySet()) {
				if (otherEntry.getKey().equals(nameOfTestPopulation)) {
					continue;
				}
				KVStore otherStore = otherEntry.getValue().get(0);
				List<Double> otherValues = otherStore.getAsDoubleList(sampledValues);
				annotateSigTestResult(otherStore, output, StatisticsUtil.mannWhitneyTwoSidedSignificance(testValues, otherValues), StatisticsUtil.mean(otherValues), StatisticsUtil.mean(testValues));
			}
		}
	}

	/**
	 * Computes a t-test for each setting comparing the best population to the others.
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleIDs The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param outputFieldName The name of the field where to store the result to.
	 */
	public static void bestTTest(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output) {
		final String bestOutput = output + "_best";
		best(collection, setting, sampleID, sampledValues, bestOutput);

		TwoLayerKVStoreCollectionPartition partition = new TwoLayerKVStoreCollectionPartition(setting, sampleID, collection);

		for (Entry<String, Map<String, KVStoreCollection>> partitionEntry : partition) {
			Optional<Entry<String, KVStoreCollection>> best = partitionEntry.getValue().entrySet().stream().filter(x -> x.getValue().get(0).getAsBoolean(bestOutput)).findFirst();
			if (best.isPresent()) {
				KVStoreCollection merged = new KVStoreCollection();
				partitionEntry.getValue().values().forEach(merged::addAll);
				tTest(merged, setting, sampleID, sampledValues, best.get().getValue().get(0).getAsString(sampleID), output);
			} else {
				logger.warn("No best population available for setting {}", partitionEntry.getKey());
			}
		}
	}

	/**
	 * Carries out a t-test (which requires the tested populations to stem from a normal distribution) to make a pair-wise 1-to-n test.
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param nameOfTestPopulation The value of the targetOfComparison field that is to be used as the 1 sample which is compared to the n other samples.
	 * @param output The name of the field where to store the result to.
	 */
	public static void tTest(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String nameOfTestPopulation, final String output) {
		KVStoreCollection grouped = new KVStoreCollection(collection);
		grouped.group(setting, sampleID);

		TwoLayerKVStoreCollectionPartition partition = new TwoLayerKVStoreCollectionPartition(setting, sampleID, grouped);

		for (Entry<String, Map<String, KVStoreCollection>> partitionEntry : partition) {
			KVStoreCollection testCollection = partitionEntry.getValue().get(nameOfTestPopulation);
			if (testCollection == null || testCollection.isEmpty()) {// skip this test as there is no population available to compare to the other populations.
				continue;
			}

			KVStore testStore = testCollection.get(0);
			double testMean = StatisticsUtil.mean(testStore.getAsDoubleList(sampledValues));
			annotateSigTestResult(testStore, output, false, 0, 0);

			for (Entry<String, KVStoreCollection> comparedEntry : partitionEntry.getValue().entrySet()) {
				if (comparedEntry.getKey().equals(nameOfTestPopulation) || comparedEntry.getValue().isEmpty()) { // skip the test population itself.
					continue;
				}
				KVStore otherStore = comparedEntry.getValue().get(0);
				annotateSigTestResult(otherStore, output, StatisticsUtil.twoSampleTTestSignificance(testStore.getAsDoubleList(sampledValues), otherStore.getAsDoubleList(sampledValues)),
						StatisticsUtil.mean(otherStore.getAsDoubleList(sampledValues)), testMean);
			}
		}
	}

	/**
	 * This method searches for the best performing KVStores and afterwards projects the collection to the subset of best KVStore per setting.
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 */
	public static void bestFilter(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues) {
		bestFilter(collection, setting, sampleID, sampledValues, DEFAULT_OUTPUT_BEST);
	}

	/**
	 * This method searches for the best performing KVStores and afterwards projects the collection to the subset of best KVStore per setting.
	 *
	 * @param collection The collection of KVStores.
	 * @param setting The field name of the setting description, e.g. dataset.
	 * @param sampleID The field name of the ids for the different populations, e.g. algorithm.
	 * @param sampledValues The field name of the values sampled from the populations, e.g. error rates.
	 * @param output The name of the field where to store the result to.
	 */
	public static void bestFilter(final KVStoreCollection collection, final String setting, final String sampleID, final String sampledValues, final String output) {
		best(collection, setting, sampleID, sampledValues, output);
		List<KVStore> distinctTasks = new ArrayList<>();
		Set<String> consideredKeys = new HashSet<>();
		collection.forEach(t -> {
			String keyValue = t.getAsString(setting);
			if (!consideredKeys.contains(keyValue) && t.getAsBoolean(output)) {
				consideredKeys.add(keyValue);
				distinctTasks.add(t);
			}
		});
		collection.clear();
		collection.addAll(distinctTasks);
	}

	/**
	 * Ensures that the provided KVStoreCollection is grouped according to the two keys and provides a two layer partition of the KVStoreCollection.
	 *
	 * @param collection The collection for which to create a partitioning.
	 * @param firstLayerKey The key name that is used to partition the first layer.
	 * @param secondLayerKey The key name that is used to partition the second layer.
	 * @return A partitioning of the given collection with respect to the first and second layer key.
	 */
	private static TwoLayerKVStoreCollectionPartition prepareGroupedTwoLayerPartition(final KVStoreCollection collection, final String firstLayerKey, final String secondLayerKey) {
		KVStoreCollection copy = new KVStoreCollection(collection);
		copy.group(firstLayerKey, secondLayerKey);
		return new TwoLayerKVStoreCollectionPartition(firstLayerKey, secondLayerKey, copy);
	}

	/**
	 * Assigns the resulting flag for the significance test to the store to be annotated.
	 *
	 * @param storeToAnnotate The KVStore where the sig test result is meant to be stored.
	 * @param output The key for the KVStore where to put the result.
	 * @param sig Whether the result was significantly different.
	 * @param meanOfStore The mean of the store to be annotated.
	 * @param meanOfCompared The mean of the store to which storeToAnnotate is compared to.
	 */
	private static void annotateSigTestResult(final KVStore storeToAnnotate, final String output, final boolean sig, final double meanOfStore, final double meanOfCompared) {
		if (sig) {
			if (meanOfStore < meanOfCompared) {
				storeToAnnotate.put(output, ESignificanceTestResult.SUPERIOR);
			} else {
				storeToAnnotate.put(output, ESignificanceTestResult.INFERIOR);
			}
		} else {
			storeToAnnotate.put(output, ESignificanceTestResult.TIE);
		}
	}

	/**
	 * Convert the two given lists into a mapping. It is assumed that same index belongst to the same mapping.
	 *
	 * @param pairingIndices The list of pairing indices, e.g., seeds.
	 * @param sampledValues The list of sampled values, e.g. measured error rate etc.
	 * @return A mapping from pairing index to sampled value.
	 */
	private static Map<String, Double> toSampleMap(final List<String> pairingIndices, final List<Double> sampledValues) {
		if (pairingIndices.size() != sampledValues.size()) {
			throw new IllegalArgumentException("Number of sample ids deviates from number of sampled values");
		}
		Map<String, Double> sampleMap = new HashMap<>();
		for (int i = 0; i < pairingIndices.size(); i++) {
			sampleMap.put(pairingIndices.get(i), sampledValues.get(i));
		}
		return sampleMap;
	}

	/**
	 * Computes a statistic of average rankings for sampleIDs.
	 *
	 * @param groupedAll The collection of KVStores to compute the average rank for the respective sampleIDs.
	 * @param sampleIDs The name of the field distinguishing the different samples.
	 * @param rank The name of the field containing the rank information.
	 * @return
	 */
	public static Map<String, DescriptiveStatistics> averageRank(final KVStoreCollection groupedAll, final String sampleIDs, final String rank) {
		Map<String, DescriptiveStatistics> averageRanks = new HashMap<>();

		for (KVStore s : groupedAll) {
			DescriptiveStatistics stats = averageRanks.get(s.getAsString(sampleIDs));
			if (stats == null) {
				stats = new DescriptiveStatistics();
				averageRanks.put(s.getAsString(sampleIDs), stats);
			}
			stats.addValue(s.getAsDouble(rank));
		}
		return averageRanks;
	}

}
