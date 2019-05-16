package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance implements IInstance {

    /** Attribute values of the instance. */
    List<IAttributeValue<?>> attributeValues;

    /** Target value of the instance. */
    IAttributeValue<?> targetValue;

    /**
     * Constructor.
     * 
     * @param dataset
     * @param attributeValues
     * @param targetValue
     */
    public TimeSeriesInstance(final List<IAttributeValue<?>> attributeValues, final IAttributeValue<?> targetValue) {
        // Set attributes.
        this.attributeValues = attributeValues;
        this.targetValue = targetValue;
        this.attributeValues = attributeValues;
    }

    @Override
    public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type) {
        return (IAttributeValue<T>) attributeValues.get(position);
    }

    @Override
    public <T> IAttributeValue<T> getTargetValue(Class<T> type) {
        return (IAttributeValue<T>) targetValue;
    }

    @Override
    public boolean removeAttributeValue(int position) {
        try {
            attributeValues.remove(position);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public int getNumberOfAttributes() {
        return attributeValues.size();
    }

    @Override
    public List<IAttributeValue<?>> getAttributeValues() {
        return attributeValues;
    }

}