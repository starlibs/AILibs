package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.apache.commons.math3.geometry.partitioning.Region.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.AILabeledAttributeArrayDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;

/**
 * This helper class provides methods that are required in order to discretize
 * numeric attributes.
 *
 * @author Felix Weiland
 *
 * @param <I>
 *            The instance type
 */
public class DiscretizationHelper<D extends AILabeledAttributeArrayDataset<?, ?>> {

	private static final Logger LOG = LoggerFactory.getLogger(DiscretizationHelper.class);

	public enum DiscretizationStrategy {
		EQUAL_LENGTH, EQUAL_SIZE
	}

	public DiscretizationHelper() {
		super();
	}

	/**
	 * This method creates a default discretization policy for each numeric
	 * attribute in the attributes that have to be considered for stratum
	 * assignment.
	 *
	 * @param dataset
	 *            The data set that has to be sampled
	 * @param indices
	 *            Indices of the attributes that have to be considered for stratum
	 *            assignment
	 * @param attributeValues
	 *            Values of the relevant attributes
	 * @param discretizationStrategy
	 *            The discretization strategy that has to be used
	 * @param numberOfCategories
	 *            The number of categories to which the numeric values have to be
	 *            assigned
	 * @return
	 */
	public Map<Integer, AttributeDiscretizationPolicy> createDefaultDiscretizationPolicies(final D dataset,
			final List<Integer> indices, final Map<Integer, Set<Object>> attributeValues,
			final DiscretizationStrategy discretizationStrategy, final int numberOfCategories) {
		Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies = new HashMap<>();

		// Only consider numeric attributes
		Set<Integer> indicesToConsider = this.getNumericIndicesFromDataset(dataset);
		indicesToConsider.retainAll(indices);
		for (int index : indicesToConsider) {
			// Get the (distinct) values in sorted order
			List<Double> numericValues = this.getSortedNumericValues(attributeValues, index);

			// No discretization needed if there are more categories than values
			if (numericValues.size() <= numberOfCategories) {
				LOG.info("No discretization policy for attribute {} needed", index);
				continue;
			}
			switch (discretizationStrategy) {
			case EQUAL_SIZE:
				discretizationPolicies.put(index, this.equalSizePolicy(numericValues, numberOfCategories));
				break;
			case EQUAL_LENGTH:
				discretizationPolicies.put(index, this.equalLengthPolicy(numericValues, numberOfCategories));
				break;
			default:
				throw new IllegalArgumentException(String.format("Invalid strategy: %s", discretizationStrategy));
			}

		}

		return discretizationPolicies;
	}

	/**
	 * Creates an equal size policy for the given values with respect to the given
	 * number of categories. An equal size policy is a policy where the length of
	 * the intervals is chosen such that in each interval there are equally many
	 * values.
	 *
	 * @param numericValues
	 *            Distinct attribute values in ascending order
	 * @param numberOfCategories
	 *            Number of categories
	 * @return The created discretization policy consisting of one interval per
	 *         category
	 */
	public AttributeDiscretizationPolicy equalSizePolicy(final List<Double> numericValues, final int numberOfCategories) {
		if (numericValues.isEmpty()) {
			throw new IllegalArgumentException("No values provided");
		}

		List<Interval> intervals = new ArrayList<>();
		int stepwidth = numericValues.size() / numberOfCategories;
		int limit = Math.min(numberOfCategories, numericValues.size());

		for (int i = 0; i < limit; i++) {
			int lower = i * stepwidth;
			int upper;
			if (i == limit - 1) {
				// Take the rest of the values
				upper = numericValues.size() - 1;
			} else {
				upper = ((i + 1) * stepwidth) - 1;
			}
			intervals.add(new Interval(numericValues.get(lower), numericValues.get(upper)));
		}

		return new AttributeDiscretizationPolicy(intervals);
	}

	/**
	 * Creates an equal length policy for the given values with respect to the given
	 * number of categories. An equal length policy is a policy where the length of
	 * the intervals is the same for all intervals.
	 *
	 * @param numericValues
	 *            Distinct attribute values in ascending order
	 * @param numberOfCategories
	 *            Number of categories
	 * @return The created discretization policy consisting of one interval per
	 *         category
	 */
	public AttributeDiscretizationPolicy equalLengthPolicy(final List<Double> numericValues, final int numberOfCategories) {
		List<Interval> intervals = new ArrayList<>();

		double max = Collections.max(numericValues);
		double min = Collections.min(numericValues);
		double stepwidth = Math.abs(max - min) / numberOfCategories;
		for (int i = 0; i < numberOfCategories; i++) {
			double lower = min + (i * stepwidth);
			double upper = min + (i + 1) * stepwidth;
			intervals.add(new Interval(lower, upper));
		}

		return new AttributeDiscretizationPolicy(intervals);
	}

	/**
	 * Returns an ascending list of attribute values for the given attribute
	 *
	 * @param attributeValues
	 * @param attributeIndex
	 * @return
	 */
	private List<Double> getSortedNumericValues(final Map<Integer, Set<Object>> attributeValues, final int attributeIndex) {
		Set<Object> values = attributeValues.get(attributeIndex);
		List<Double> toReturn = new ArrayList<>();
		values.forEach(v -> toReturn.add((Double) v));
		Collections.sort(toReturn);
		return toReturn;
	}

	/**
	 * Returns the set of attribute indices belonging to numeric attributes
	 *
	 * @param dataset
	 * @return
	 */
	private Set<Integer> getNumericIndicesFromDataset(final D dataset) {
		Set<Integer> numericAttributes = new HashSet<>();
		List<IAttributeType<?>> attributeTypes = new ArrayList<>(dataset.getAttributeTypes());
		attributeTypes.add(dataset.getTargetType());
		for (int i = 0; i < attributeTypes.size(); i++) {
			IAttributeType<?> attributeType = attributeTypes.get(i);
			if (attributeType instanceof NumericAttributeType) {
				numericAttributes.add(i);
			}
		}
		return numericAttributes;
	}

	/**
	 * Discretizes the given attribute values with respect to the provided policies
	 *
	 * @param discretizationPolicies
	 * @param attributeValues
	 */
	protected void discretizeAttributeValues(final Map<Integer, AttributeDiscretizationPolicy> discretizationPolicies,
			final Map<Integer, Set<Object>> attributeValues) {
		Set<Integer> numericIndices = discretizationPolicies.keySet();
		for (int index : numericIndices) {
			Set<Object> originalValues = attributeValues.get(index);
			Set<Object> discretizedValues = new HashSet<>();
			for (Object value : originalValues) {
				double d = (double) value;
				discretizedValues.add(this.discretize(d, discretizationPolicies.get(index)));
			}
			LOG.info("Attribute index {}: Reduced values from {} to {}", index, originalValues.size(),
					discretizedValues.size());
			attributeValues.put(index, discretizedValues);
		}
	}

	/**
	 * Discretizes the particular provided value. Discretization in this case means
	 * to replace the original value by a categorical value. The categorical value
	 * is simply the index of the interval the value was assigned to.
	 *
	 * @param value
	 *            The (numeric) value to be discretized
	 * @param policy
	 *            The policy that has to be used for discretization
	 * @return
	 */
	protected int discretize(final double value, final AttributeDiscretizationPolicy policy) {
		List<Interval> intervals = policy.getIntervals();
		// Find the interval to which the value belongs
		for (Interval i : intervals) {
			if (i.checkPoint(value, 0) != Location.OUTSIDE) {
				return intervals.indexOf(i);
			}
		}

		throw new IllegalStateException(String.format("Policy does not cover value %f", value));
	}

}
