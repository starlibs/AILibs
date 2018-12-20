package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance implements IInstance {
    /**
     * Dataset that this instance is part of. If not null, than the time series
     * attribute values hold a view on a row of the correnspondig matrix of the
     * dataset.
     */
    TimeSeriesDataset dataset = null;

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
    public TimeSeriesInstance(final TimeSeriesDataset dataset, final List<IAttributeValue<?>> attributeValues,
            final IAttributeValue<?> targetValue) {
        // Set attributes.
        this.dataset = dataset;
        this.attributeValues = attributeValues;
        this.targetValue = targetValue;
        this.attributeValues = attributeValues;
        // Add instance to dataset. The dataset sets a view for all
        // TimeSeriesAttributes of the instance.
        // dataset.add(this);
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