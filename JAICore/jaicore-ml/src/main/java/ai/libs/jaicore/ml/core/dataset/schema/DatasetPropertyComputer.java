package ai.libs.jaicore.ml.core.dataset.schema;

import java.util.ArrayList;
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

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DatasetPropertyComputer {

	private DatasetPropertyComputer() {
		/* avoids instantiation */
	}

	private static final Logger LOG = LoggerFactory.getLogger(DatasetPropertyComputer.class);

	public static Map<Integer, Set<Object>> computeAttributeValues(final IDataset<?> dataset) {
		List<Integer> allAttributeIndices = new ArrayList<>();
		int n = dataset.getNumAttributes();
		for (int i = 0; i < n; i++) {
			allAttributeIndices.add(i);
		}
		return computeAttributeValues(dataset, allAttributeIndices, 1);
	}

	/**
	 * This method computes for each desired attribute the set of occurring values.
	 * If numCPU > 1, the computation is done in parallel.
	 */
	public static Map<Integer, Set<Object>> computeAttributeValues(final IDataset<?> dataset, final List<Integer> pAttributeIndices, final int numCPUs) {
		LOG.info("computeAttributeValues(): enter");
		Map<Integer, Set<Object>> attributeValues = new HashMap<>();

		// SCALE-54: Use target attribute only if no attribute indices are provided
		int targetIndex = -1;
		List<Integer> attributeIndices;
		if (dataset instanceof ILabeledDataset<?>) {
			// We assume that the last attribute is the target attribute
			targetIndex = dataset.getNumAttributes();
			if ((pAttributeIndices == null || pAttributeIndices.isEmpty())) {
				if (LOG.isInfoEnabled()) {
					LOG.info(String.format("No attribute indices provided. Working with target attribute only (index: %d", targetIndex));
				}
				attributeIndices = Collections.singletonList(targetIndex);
			}
			else {
				attributeIndices = new ArrayList<>(pAttributeIndices);
				attributeIndices.add(targetIndex);
			}
		}
		else {
			attributeIndices = new ArrayList<>(pAttributeIndices);
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Computing attribute values for attribute indices {}", attributeIndices);
		}

		// Check validity of the attribute indices
		for (int attributeIndex : attributeIndices) {
			if (attributeIndex > dataset.getNumAttributes()) {
				throw new IndexOutOfBoundsException(String.format("Attribute index %d is out of bounds for the delivered data set!", attributeIndex));
			}
		}

		// Setup map with empty sets
		for (int attributeIndex : attributeIndices) {
			attributeValues.put(attributeIndex, new HashSet<>());
		}

		/* partitions the dataset and computes, in parallel, all the values that exist in each partition for each attribute */
		ExecutorService threadPool = Executors.newFixedThreadPool(numCPUs);
		List<Future<Map<Integer, Set<Object>>>> futures = new ArrayList<>();
		if (LOG.isInfoEnabled()) {
			LOG.info(String.format("Starting %d threads for computation..", numCPUs));
		}
		int listSize = dataset.size() / numCPUs;
		for (List<? extends IInstance> sublist : Lists.partition(dataset, listSize)) {
			futures.add(threadPool.submit(new ListProcessor<>(sublist, new HashSet<>(attributeIndices), dataset)));
		}
		threadPool.shutdown(); // blocks until all computations are ready

		/* now merge the results of the partitions into a global set of existing values for each attribute */
		for (Future<Map<Integer, Set<Object>>> future : futures) {
			try {
				// Merge locally computed attribute values into the global list
				Map<Integer, Set<Object>> localAttributeValues = future.get();
				for (Entry<Integer, Set<Object>> entry : attributeValues.entrySet()) {
					IAttribute att = entry.getKey() == targetIndex ? ((ILabeledDataset<?>)dataset).getLabelAttribute() : dataset.getAttribute(entry.getKey());
					for (Object o : entry.getValue()) {
						if (!att.isValidValue(o)) {
							throw new IllegalStateException("Collecting invalid value " + o + " for attribute " + att + ". Valid values: " + att.getStringDescriptionOfDomain());
						}
					}
					attributeValues.get(entry.getKey()).addAll(localAttributeValues.get(entry.getKey()));
				}
			} catch (ExecutionException e) {
				LOG.error("Exception while waiting for future to complete..", e);
			} catch (InterruptedException e) {
				LOG.error("Thread has been interrupted");
				Thread.currentThread().interrupt();
			}
		}
		return attributeValues;
	}

	/**
	 * Helper class which processes a sublist of the original data set and collects
	 * the occurring attribute values on this sublist.
	 *
	 * @author Felix Weiland
	 *
	 */
	static class ListProcessor<D extends IDataset<?>> implements Callable<Map<Integer, Set<Object>>> {

		private List<? extends IInstance> list;

		private Set<Integer> attributeIndices;

		private D dataset;

		public ListProcessor(final List<? extends IInstance> list, final Set<Integer> attributeIndices, final D dataset) {
			super();
			this.list = list;
			this.attributeIndices = attributeIndices;
			this.dataset = dataset;
		}

		@Override
		public Map<Integer, Set<Object>> call() {
			if (LOG.isInfoEnabled()) {
				LOG.info(String.format("Starting computation on local sublist of length %d", this.list.size()));
			}

			// Setup local map
			Map<Integer, Set<Object>> attributeValues = new HashMap<>();

			// Initialize local map with empty sets
			for (int attributeIndex : this.attributeIndices) {
				attributeValues.put(attributeIndex, new HashSet<>());
			}

			// Collect attribute values
			for (IInstance instance : this.list) {
				for (int attributeIndex : this.attributeIndices) {
					if (attributeIndex == this.dataset.getNumAttributes()) {
						// Attribute index describes target attribute
						Object label = ((ILabeledInstance)instance).getLabel();
						if (label != null) {
							attributeValues.get(attributeIndex).add(label);
						}
					} else {
						Object value = instance.getAttributeValue(attributeIndex);
						if (value != null) {
							attributeValues.get(attributeIndex).add(value);
						}
					}
				}
			}

			LOG.info("Finished local computation");

			return attributeValues;
		}
	}
}