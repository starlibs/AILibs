package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance<L> implements ILabeledAttributeArrayInstance<L> {

	/** Attribute values of the instance. */
	private IAttributeValue<?>[] attributeValues;

	/** Target value of the instance. */
	private L targetValue;

	/**
	 * Constructor.
	 *
	 * @param dataset
	 * @param attributeValues
	 * @param targetValue
	 */
	public TimeSeriesInstance(final IAttributeValue<?>[] attributeValues, final L targetValue) {
		// Set attributes.
		this.attributeValues = attributeValues;
		this.targetValue = targetValue;
	}

	public TimeSeriesInstance(final List<IAttributeValue<?>> attributeValues, final L targetValue) {
		int n = attributeValues.size();
		this.attributeValues = new IAttributeValue<?>[n];
		for (int i = 0; i < n; i++) {
			this.attributeValues[i] = attributeValues.get(i);
		}
		this.targetValue = targetValue;
	}

	@Override
	public <T> IAttributeValue<T> getAttributeValueAtPosition(final int position, final Class<T> type) {
		return (IAttributeValue<T>) this.attributeValues[position];
	}

	@Override
	public L getTargetValue() {
		return this.targetValue;
	}

	@Override
	public int getNumberOfAttributes() {
		return this.attributeValues.length;
	}

	@Override
	public IAttributeValue<?>[] getAllAttributeValues() {
		return this.attributeValues;
	}
}