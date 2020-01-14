package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.common.control.ILoggingCustomizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import ai.libs.jaicore.ml.core.dataset.schema.DatasetPropertyComputer;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.DiscretizationHelper.DiscretizationStrategy;

/**
 * This class is responsible for computing the amount of strati in
 * attribute-based stratified sampling and assigning elements to the strati.
 *
 * @author Felix Weiland
 *
 */
public class AttributeBasedStratiAmountSelectorAndAssigner implements IStratiAmountSelector, IStratiAssigner, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(AttributeBasedStratiAmountSelectorAndAssigner.class);

	/** Default strategy for discretization */
	private static final DiscretizationStrategy DEFAULT_DISCRETIZATION_STRATEGY = DiscretizationStrategy.EQUAL_SIZE;

	private final DiscretizationHelper discretizationHelper = new DiscretizationHelper();

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
	private Map<List<Object>, Integer> stratumIDs;

	/** Number of CPU cores to be used */
	private int numCPUs = 1;

	/** The data set which has to be sampled */
	private IDataset<?> dataset;

	private int numAttributes;

	/** Policies for discretization */
	private Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies;

	/** The discretization strategy selected by the user */
	private DiscretizationStrategy discretizationStrategy;

	/** The number of categories for discretization selected by the user */
	private int numberOfCategories;

	private boolean initialized;

	/**
	 * SCALE-54: Explicitly allow to not provide an attribute list
	 */
	public AttributeBasedStratiAmountSelectorAndAssigner() {
		super();
		this.discretizationStrategy = DEFAULT_DISCRETIZATION_STRATEGY;
		this.numberOfCategories = DEFAULT_DISCRETIZATION_CATEGORY_AMOUNT;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(final List<Integer> attributeIndices) {
		this(attributeIndices, null);
		this.discretizationStrategy = DEFAULT_DISCRETIZATION_STRATEGY;
		this.numberOfCategories = DEFAULT_DISCRETIZATION_CATEGORY_AMOUNT;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(final List<Integer> attributeIndices, final DiscretizationStrategy discretizationStrategy, final int numberOfCategories) {
		this(attributeIndices, null);
		this.discretizationStrategy = discretizationStrategy;
		this.numberOfCategories = numberOfCategories;
	}

	public AttributeBasedStratiAmountSelectorAndAssigner(final List<Integer> attributeIndices, final Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies) {
		super();
		// Validate attribute indices
		if (attributeIndices == null || attributeIndices.isEmpty()) {
			throw new IllegalArgumentException("No attribute indices are provided!");
		}
		this.attributeIndices = attributeIndices;
		this.discretizationPolicies = discretizationPolicies;
		this.logger.info("Created assigner. Attributes to be discretized: {}", discretizationPolicies == null ? "none" : discretizationPolicies.keySet());
	}

	@Override
	public int selectStratiAmount(final IDataset<?> dataset) {
		this.logger.debug("Selecting number of strati for dataset with {} items.", dataset.size());
		if (this.dataset == null) {
			this.init(dataset, -1); // the strati amount is ignored here anyway since being computed automatically
		} else if (!this.dataset.equals(dataset)) {
			throw new IllegalArgumentException("Can only select strati amount for a dataset provided before.");
		}
		return this.stratumIDs.size();
	}

	private void discretizeAttributeValues(final Map<Integer, Set<Object>> attributeValues) {
		if (this.discretizationPolicies == null) {
			this.logger.info("No discretization policies provided. Computing defaults.");
			this.discretizationPolicies = this.discretizationHelper.createDefaultDiscretizationPolicies(this.dataset, this.attributeIndices, attributeValues, this.discretizationStrategy, this.numberOfCategories);
		}

		if (!this.discretizationPolicies.isEmpty()) {
			if (this.logger.isInfoEnabled()) {
				this.logger.info("Discretizing numeric attributes using policies: {}", this.discretizationPolicies);
			}
			this.discretizationHelper.discretizeAttributeValues(this.discretizationPolicies, attributeValues);
		}

		this.logger.info("computeAttributeValues(): leave");
	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		if (numberOfCPUs < 1) {
			throw new IllegalArgumentException("Number of CPU cores must be nonnegative");
		}
		this.numCPUs = numberOfCPUs;
	}

	@Override
	public int getNumCPUs() {
		return this.numCPUs;
	}

	public void init(final IDataset<?> dataset) {
		this.init(dataset, -1);
	}

	@Override
	public void init(final IDataset<?> dataset, final int stratiAmount) {

		this.logger.debug("init(): enter");

		/* first, conduct some consistency checks */
		if (this.initialized) {
			this.logger.warn("Ignoring further initialization.");
			return;
		}
		if (dataset == null) {
			throw new IllegalArgumentException("Cannot set dataset to NULL");
		}
		this.dataset = dataset;
		this.numAttributes = dataset.getNumAttributes();

		/* consistency check of attribute indices */
		int n = dataset.getNumAttributes();
		for (int i : this.attributeIndices) {
			if (i < 0) {
				throw new IllegalArgumentException("Attribute index for stratified splits must not be negative!");
			}
			if (i > n) {
				throw new IllegalArgumentException("Attribute index for stratified splits must not exceed number of attributes!");
			}
			if (i == n && !(dataset instanceof ILabeledDataset)) {
				throw new IllegalArgumentException("Attribute index for stratified splits must only equal the number of attributes if the dataset is labeled, because then the label column id is the number of attributes!");
			}
		}

		/* now compute the set of strati labels. There is one stratum for each element in the cartesian product of
		 * all possible combinations of (maybe discretized) values for the given attribute indices */
		Map<Integer, Set<Object>> attributeValues = DatasetPropertyComputer.computeAttributeValues(dataset, this.attributeIndices, this.numCPUs);
		this.discretizeAttributeValues(attributeValues);
		List<Set<Object>> sets = new ArrayList<>(attributeValues.values());
		Set<List<Object>> cartesianProduct = Sets.cartesianProduct(sets);
		this.logger.info("There are {} elements in the cartesian product of the attribute values", cartesianProduct.size());

		/* now assign an ID to each stratum. We do not use a list here for more effective look-up later */
		this.logger.info("Assigning stratum numbers to elements in the cartesian product..");
		this.stratumIDs = new HashMap<>();
		int stratumCounter = 0;
		for (List<Object> tuple : cartesianProduct) {
			this.stratumIDs.put(tuple, stratumCounter++);
		}

		this.logger.info("Initialized strati assigner with {} strati.", this.stratumIDs.size());
		this.initialized = true;
	}

	@Override
	public int assignToStrati(final IInstance datapoint) {
		if (!this.initialized) {
			throw new IllegalStateException("Assigner has not been initialized yet.");
		}

		// Compute concrete attribute values relevant for the stratum for the particular instance
		List<Object> instanceAttributeValues = new ArrayList<>(this.attributeIndices.size());
		for (int i = 0; i < this.attributeIndices.size(); i++) {
			int attributeIndex = this.attributeIndices.get(i);
			Object value;
			// Has value to be discretized?
			if (this.toBeDiscretized(attributeIndex)) {
				Object raw;
				if (attributeIndex == this.dataset.getNumAttributes()) { // this can only happen for labeled instances
					raw = ((ILabeledInstance) datapoint).getLabel();
				} else {
					raw = datapoint.getAttributeValue(attributeIndex);
				}
				value = this.discretizationHelper.discretize((double) raw, this.discretizationPolicies.get(attributeIndex));
				Objects.requireNonNull(value);
			} else {
				if (attributeIndex == this.numAttributes) { // this can only happen for labeled instances
					value = ((ILabeledInstance) datapoint).getLabel();
					if (value == null) {
						throw new IllegalArgumentException("Cannot assign data point " + datapoint + " to any stratum, because it has no label.");
					}
				} else {
					value = datapoint.getAttributeValue(attributeIndex);
					Objects.requireNonNull(value);
				}
			}
			instanceAttributeValues.add(value);
		}
		int stratum = this.stratumIDs.get(instanceAttributeValues);
		this.logger.debug("Attribute values are: {}. Corresponding stratum is: {}", instanceAttributeValues, stratum);
		return stratum;
	}

	private boolean toBeDiscretized(final int index) {
		return this.discretizationPolicies.containsKey(index);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
		this.discretizationHelper.setLoggerName(name + ".discretizer");
	}
}
