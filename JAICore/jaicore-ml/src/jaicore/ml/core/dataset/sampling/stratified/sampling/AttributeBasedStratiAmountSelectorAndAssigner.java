package jaicore.ml.core.dataset.sampling.stratified.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * This class is responsible for computing the amount of strati in
 * attribute-based stratified sampling and assigning elements to the strati.
 * 
 * @author Felix Weiland
 *
 */
public class AttributeBasedStratiAmountSelectorAndAssigner<I extends IInstance>
		implements IStratiAmountSelector<I>, IStratiAssigner<I> {

	private static Logger LOG = LoggerFactory.getLogger(AttributeBasedStratiAmountSelectorAndAssigner.class);

	private List<Integer> attributeIndices;

	private MultiKeyMap<Object, Integer> stratumAssignments;

	private int numCPUs = 1;

	private IDataset<I> dataset;

	public AttributeBasedStratiAmountSelectorAndAssigner(List<Integer> attributeIndices) {
		super();
		if (attributeIndices == null || attributeIndices.isEmpty()) {
			throw new IllegalArgumentException("No attribute indices are provided!");
		}
		this.attributeIndices = attributeIndices;
	}

	@Override
	public int selectStratiAmount(IDataset<I> dataset) {
		Map<Integer, Set<Object>> attributeValues = this.getAttributeValues(dataset);
		// Number of strati is size of the cartesian product of all attribute values
		int noStrati = 1;
		for (Set<Object> values : attributeValues.values()) {
			noStrati *= values.size();
		}
		LOG.info(String.format("%d strati are needed", noStrati));
		return noStrati;
	}

	public Map<Integer, Set<Object>> getAttributeValues(IDataset<I> dataset) {
		LOG.info("getAttributeValues(): enter");
		LOG.debug("Computing attribute values for attribute indices {}", attributeIndices.toString());

		// Check validity of the attribute indices
		for (int attributeIndex : attributeIndices) {
			if (attributeIndex > dataset.getNumberOfAttributes()) {
				throw new IndexOutOfBoundsException(String
						.format("Attribute index %d is out of bounds for the delivered data set!", attributeIndex));
			}
		}

		// Map containing for each attribute the set of possible values
		Map<Integer, Set<Object>> attributeValues = new HashMap<>();

		// Setup map with empty sets
		for (int attributeIndex : attributeIndices) {
			attributeValues.put(attributeIndex, new HashSet<>());
		}

		// Setup parallel computation
		ExecutorService threadPool = Executors.newFixedThreadPool(numCPUs);
		List<Future<Map<Integer, Set<Object>>>> futures = new ArrayList<>();

		// Start threads
		LOG.info(String.format("Starting %d threads for computation..", this.numCPUs));
		int listSize = dataset.size() / this.numCPUs;
		for (List<I> sublist : Lists.partition(dataset, listSize)) {
			futures.add(threadPool.submit(new ListProcessor<I>(sublist, new HashSet<>(attributeIndices), dataset)));
		}

		// Collect results
		for (Future<Map<Integer, Set<Object>>> future : futures) {
			try {
				// Merge locally computed attribute values into the global list
				Map<Integer, Set<Object>> localAttributeValues = future.get();
				for (int attributeIndex : attributeValues.keySet()) {
					attributeValues.get(attributeIndex).addAll(localAttributeValues.get(attributeIndex));
				}
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Exception while waiting for future to complete..", e);
			}
		}

		// Finish parallel computation
		threadPool.shutdown();

		LOG.info("getAttributeValues(): leave");
		return attributeValues;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		if (numberOfCPUs < 1) {
			throw new IllegalArgumentException("Number of CPU cores must be nonnegative");
		}
		this.numCPUs = numberOfCPUs;
	}

	@Override
	public int getNumCPUs() {
		return this.numCPUs;
	}

	@Override
	public void init(IDataset<I> dataset, int stratiAmount) {
		// stratiAmount is not used here since it is computed dynamically
		init(dataset);
	}

	public void init(IDataset<I> dataset) {
		LOG.debug("init(): enter");
		this.dataset = dataset;

		Map<Integer, Set<Object>> attributeValues = this.getAttributeValues(dataset);

		// Each set represents the set of possible attribute values
		List<Set<Object>> sets = new ArrayList<Set<Object>>(attributeValues.values());
		Set<List<Object>> cartesianProducts = Sets.cartesianProduct(sets);
		stratumAssignments = new MultiKeyMap<>();

		LOG.info("There are {} elements in the cartesian product of the attribute values", cartesianProducts.size());

		LOG.info("Assigning stratum numbers to elements in the cartesian product..");
		// TODO: Could this be done in parallel?
		// Add mapping for each element in the Cartesian product
		int stratumCounter = 0;
		for (List<Object> cartesianProduct : cartesianProducts) {
			Object[] arr = new Object[cartesianProduct.size()];
			cartesianProduct.toArray(arr);
			MultiKey<Object> multiKey = new MultiKey<>(arr);
			if (stratumAssignments.containsKey(multiKey)) {
				throw new RuntimeException(String.format("Mulitkey %s occured twice!", multiKey.toString()));
			}
			stratumAssignments.put(new MultiKey<>(arr), stratumCounter++);
		}

		LOG.debug("init(): leave");
	}

	@Override
	public int assignToStrati(IInstance datapoint) {
		if (stratumAssignments == null || stratumAssignments.isEmpty()) {
			throw new IllegalStateException("StratiAssigner has not been initialized!");
		}

		// Compute concrete attribute values for the particular instance
		Object[] attributeValues = new Object[attributeIndices.size()];
		for (int i = 0; i < attributeIndices.size(); i++) {
			int attributIndex = attributeIndices.get(i);
			if (attributIndex == dataset.getNumberOfAttributes()) {
				attributeValues[i] = datapoint.getTargetValue(Object.class).getValue();
			} else {
				attributeValues[i] = datapoint.getAttributeValue(attributIndex, Object.class).getValue();
			}
		}
		LOG.debug(String.format("Attribute values are: %s", Arrays.toString(attributeValues)));

		// Request mapping for the concrete attribute values
		MultiKey<Object> multiKey = new MultiKey<>(attributeValues);
		if (!stratumAssignments.containsKey(multiKey)) {
			throw new RuntimeException(String.format("No assignment available for attribute combination %s",
					Arrays.toString(attributeValues)));
		}

		int stratum = stratumAssignments.get(multiKey);

		LOG.debug("Assigned stratum {}", stratum);
		return stratum;
	}

}

/**
 * Helper class which processes a sublist of the original data set and collects
 * the occurring attribute values on this sublist.
 * 
 * @author Felix Weiland
 *
 */
class ListProcessor<I extends IInstance> implements Callable<Map<Integer, Set<Object>>> {

	private static Logger LOG = LoggerFactory.getLogger(ListProcessor.class);

	private List<I> list;

	private Set<Integer> attributeIndices;

	private IDataset<I> dataset;

	public ListProcessor(List<I> list, Set<Integer> attributeIndices, IDataset<I> dataset) {
		super();
		this.list = list;
		this.attributeIndices = attributeIndices;
		this.dataset = dataset;
	}

	@Override
	public Map<Integer, Set<Object>> call() {
		LOG.info(String.format("Starting computation on local sublist of length %d", list.size()));

		// Setup local map
		Map<Integer, Set<Object>> attributeValues = new HashMap<>();

		// Initialize local map with empty sets
		for (int attributeIndex : attributeIndices) {
			attributeValues.put(attributeIndex, new HashSet<>());
		}

		// Collect attribute values
		for (IInstance instance : list) {
			for (int attributeIndex : attributeIndices) {
				if (attributeIndex == dataset.getNumberOfAttributes()) {
					// Attribute index describes target attribute
					attributeValues.get(attributeIndex).add(instance.getTargetValue(Object.class).getValue());

				} else {
					attributeValues.get(attributeIndex)
							.add(instance.getAttributeValue(attributeIndex, Object.class).getValue());
				}
			}
		}

		LOG.info("Finished local computation");

		return attributeValues;
	}

}
