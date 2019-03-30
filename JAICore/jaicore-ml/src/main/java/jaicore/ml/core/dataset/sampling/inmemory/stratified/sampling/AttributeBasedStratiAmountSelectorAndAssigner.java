package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;

/**
 * This class is responsible for computing the amount of strati in
 * attribute-based stratified sampling and assigning elements to the strati.
 * 
 * @author Felix Weiland
 *
 */
public class AttributeBasedStratiAmountSelectorAndAssigner<I extends IInstance>
		implements IStratiAmountSelector<I>, IStratiAssigner<I> {

	private static final Logger LOG = LoggerFactory.getLogger(AttributeBasedStratiAmountSelectorAndAssigner.class);

	/** Default strategy for discretization */
	private static final DiscretizationStrategy DEFAULT_DISCRETIZATION_STRATEGY = DiscretizationStrategy.EQUAL_SIZE;

	/**
	 * Default number of categories to be used for discretization
	 */
	private static final int DEFAULT_DISCRETIZATION_CATEGORY_AMOUNT = 5;

	/**
	 * Indices of attributes that have to be taken into account for stratum
	 * assignment
	 */
	private List<Integer> attributeIndices;

	/** Map from attribute values to stratum id */
	private MultiKeyMap<Object, Integer> stratumAssignments;

	/** Number of CPU cores to be used */
	private int numCPUs = 1;

	/** The data set which has to be sampled */
	private IDataset<I> dataset;

	/** Policies for discretization */
	private Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies;

	/**
	 * Concrete values of the attributes that have to be considered for stratum
	 * assignment
	 */
	private Map<Integer, Set<Object>> attributeValues;

	/** The discretization strategy selected by the user */
	private DiscretizationStrategy discretizationStrategy;

	/** The number of categories for discretization selected by the user */
	private int numberOfCategories;

	/**
	 * SCALE-54: Explicitly allow to not provide an attribute list
	 */
	public AttributeBasedStratiAmountSelectorAndAssigner() {
		super();
		this.discretizationStrategy = DEFAULT_DISCRETIZATION_STRATEGY;
		this.numberOfCategories = DEFAULT_DISCRETIZATION_CATEGORY_AMOUNT;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(List<Integer> attributeIndices) {
		this(attributeIndices, null);
		this.discretizationStrategy = DEFAULT_DISCRETIZATION_STRATEGY;
		this.numberOfCategories = DEFAULT_DISCRETIZATION_CATEGORY_AMOUNT;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(List<Integer> attributeIndices,
			DiscretizationStrategy discretizationStrategy, int numberOfCategories) {
		this(attributeIndices, null);
		this.discretizationStrategy = discretizationStrategy;
		this.numberOfCategories = numberOfCategories;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(List<Integer> attributeIndices,
			Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies) {
		super();
		// Validate attribute indices
		if (attributeIndices == null || attributeIndices.isEmpty()) {
			throw new IllegalArgumentException("No attribute indices are provided!");
		}
		this.attributeIndices = attributeIndices;
		this.discretizationPolicies = discretizationPolicies;
	}

	@Override
	public int selectStratiAmount(IDataset<I> dataset) {
		this.dataset = dataset;

		// Compute attribute values from data set
		computeAttributeValues();

		// Number of strati is size of the Cartesian product of all attribute values
		int noStrati = 1;
		for (Set<Object> values : this.attributeValues.values()) {
			noStrati *= values.size();
		}
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("%d strati are needed", noStrati));
		}
		return noStrati;
	}

	/**
	 * This method computes for each attribute that has to be considered for stratum
	 * assignment the set of occurring values. If numCPU > 1, the computation is
	 * done in parallel. If numeric attributes have to be considered, they are
	 * discretized subsequent to the collection of the values.
	 */
	private void computeAttributeValues() {
		LOG.info("computeAttributeValues(): enter");

		// SCALE-54: Use target attribute only if no attribute indices are provided
		if (this.attributeIndices == null || this.attributeIndices.isEmpty()) {
			// We assume that the last attribute is the target attribute
			int targetIndex = this.dataset.getNumberOfAttributes();
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("No attribute indices provided. Working with target attribute only (index: %d",
						targetIndex));
			}
			this.attributeIndices = Collections.singletonList(targetIndex);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Computing attribute values for attribute indices {}", attributeIndices);
		}

		// Check validity of the attribute indices
		for (int attributeIndex : attributeIndices) {
			if (attributeIndex > dataset.getNumberOfAttributes()) {
				throw new IndexOutOfBoundsException(String
						.format("Attribute index %d is out of bounds for the delivered data set!", attributeIndex));
			}
		}

		// Map containing for each attribute the set of possible values
		this.attributeValues = new HashMap<>();

		// Setup map with empty sets
		for (int attributeIndex : attributeIndices) {
			attributeValues.put(attributeIndex, new HashSet<>());
		}

		// Setup parallel computation
		ExecutorService threadPool = Executors.newFixedThreadPool(numCPUs);
		List<Future<Map<Integer, Set<Object>>>> futures = new ArrayList<>();

		// Start threads
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Starting %d threads for computation..", this.numCPUs));
		}
		int listSize = dataset.size() / this.numCPUs;
		for (List<I> sublist : Lists.partition(dataset, listSize)) {
			futures.add(threadPool.submit(new ListProcessor<I>(sublist, new HashSet<>(attributeIndices), dataset)));
		}

		// Collect results
		for (Future<Map<Integer, Set<Object>>> future : futures) {
			try {
				// Merge locally computed attribute values into the global list
				Map<Integer, Set<Object>> localAttributeValues = future.get();
				for (Entry<Integer, Set<Object>> entry : attributeValues.entrySet()) {
					attributeValues.get(entry.getKey()).addAll(localAttributeValues.get(entry.getKey()));
				}
			} catch (ExecutionException e) {
				LOG.error("Exception while waiting for future to complete..", e);
			} catch (InterruptedException e) {
				LOG.error("Thread has been interrupted");
				Thread.currentThread().interrupt();
			}
		}

		// Finish parallel computation
		threadPool.shutdown();

		// Discretize
		DiscretizationHelper<I> discretizationHelper = new DiscretizationHelper<>();

		if (this.discretizationPolicies == null) {
			LOG.info("No discretization policies provided. Computing defaults..");
			this.discretizationPolicies = discretizationHelper.createDefaultDiscretizationPolicies(this.dataset,
					attributeIndices, attributeValues, discretizationStrategy, numberOfCategories);
		}

		if (!discretizationPolicies.isEmpty()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Discretizing numeric attributes using policies: {}", discretizationPolicies);
			}
			discretizationHelper.discretizeAttributeValues(discretizationPolicies, attributeValues);
		}

		LOG.info("computeAttributeValues(): leave");
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

	/**
	 * Initializes the algorithm for stratum assignment.
	 * 
	 * @param dataset
	 */
	public void init(IDataset<I> dataset) {
		LOG.debug("init(): enter");

		if (this.dataset != null && this.dataset.equals(dataset) && attributeValues != null) {
			LOG.info("No recomputation of the attribute values needed");
		} else {
			this.dataset = dataset;
			computeAttributeValues();
		}

		// Each set represents the set of possible attribute values
		List<Set<Object>> sets = new ArrayList<>(attributeValues.values());
		Set<List<Object>> cartesianProducts = Sets.cartesianProduct(sets);
		stratumAssignments = new MultiKeyMap<>();

		LOG.info("There are {} elements in the cartesian product of the attribute values", cartesianProducts.size());

		LOG.info("Assigning stratum numbers to elements in the cartesian product..");
		// Add mapping for each element in the Cartesian product
		int stratumCounter = 0;
		for (List<Object> cartesianProduct : cartesianProducts) {
			Object[] arr = new Object[cartesianProduct.size()];
			cartesianProduct.toArray(arr);
			MultiKey<Object> multiKey = new MultiKey<>(arr);
			if (stratumAssignments.containsKey(multiKey)) {
				throw new IllegalStateException(String.format("Mulitkey %s occured twice!", multiKey.toString()));
			}
			stratumAssignments.put(new MultiKey<>(arr), stratumCounter++);
		}

		LOG.debug("init(): leave");
	}

	@Override
	public int assignToStrati(IInstance datapoint) {
		DiscretizationHelper<I> discretizationHelper = new DiscretizationHelper<>();

		if (stratumAssignments == null || stratumAssignments.isEmpty()) {
			throw new IllegalStateException("StratiAssigner has not been initialized!");
		}

		// Compute concrete attribute values for the particular instance
		Object[] instanceAttributeValues = new Object[attributeIndices.size()];
		for (int i = 0; i < attributeIndices.size(); i++) {
			int attributeIndex = attributeIndices.get(i);

			Object value;
			// Has value to be discretized?
			if (toBeDiscretized(attributeIndex)) {
				Object raw;
				if (attributeIndex == dataset.getNumberOfAttributes()) {
					raw = datapoint.getTargetValue(Object.class).getValue();
				} else {
					raw = datapoint.getAttributeValue(attributeIndex, Object.class).getValue();
				}
				value = discretizationHelper.discretize((double) raw, discretizationPolicies.get(attributeIndex));
			} else {
				if (attributeIndex == dataset.getNumberOfAttributes()) {
					value = datapoint.getTargetValue(Object.class).getValue();
				} else {
					value = datapoint.getAttributeValue(attributeIndex, Object.class).getValue();
				}
			}

			instanceAttributeValues[i] = value;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Attribute values are: %s", Arrays.toString(instanceAttributeValues)));
		}

		// Request mapping for the concrete attribute values
		MultiKey<Object> multiKey = new MultiKey<>(instanceAttributeValues);
		if (!stratumAssignments.containsKey(multiKey)) {
			throw new IllegalStateException(String.format("No assignment available for attribute combination %s",
					Arrays.toString(instanceAttributeValues)));
		}

		int stratum = stratumAssignments.get(multiKey);

		LOG.debug("Assigned stratum {}", stratum);
		return stratum;
	}

	private boolean toBeDiscretized(int index) {
		return this.discretizationPolicies.containsKey(index);
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

	private static final Logger LOG = LoggerFactory.getLogger(ListProcessor.class);

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
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Starting computation on local sublist of length %d", list.size()));
		}

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
