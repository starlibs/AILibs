package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance implements ILabeledAttributeArrayInstance {

    /** Attribute values of the instance. */
    IAttributeValue<?>[] attributeValues;

    /** Target value of the instance. */
    IAttributeValue<?> targetValue;

    /**
     * Constructor.
     * 
     * @param dataset
     * @param attributeValues
     * @param targetValue
     */
    public TimeSeriesInstance(final IAttributeValue<?>[] attributeValues, final IAttributeValue<?> targetValue) {
        // Set attributes.
        this.attributeValues = attributeValues;
        this.targetValue = targetValue;
    }
    
    public TimeSeriesInstance(final List<IAttributeValue<?>> attributeValues, final IAttributeValue<?> targetValue) {
    	int n = attributeValues.size();
    	this.attributeValues = new IAttributeValue<?>[n];
        for (int i = 0; i < n; i++) {
        	this.attributeValues[i] = attributeValues.get(i);
        }
        this.targetValue = targetValue;
    }

    @Override
    public <T> IAttributeValue<T> getAttributeValueAtPosition(int position, Class<T> type) {
        return (IAttributeValue<T>) attributeValues[position];
    }

    @Override
    public <T> IAttributeValue<T> getTargetValue(Class<T> type) {
        return (IAttributeValue<T>) targetValue;
    }

    @Override
    public int getNumberOfAttributes() {
        return attributeValues.length;
    }

	@Override
	public IAttributeValue<?>[] getAllAttributeValues() {
		return attributeValues;
	}
}