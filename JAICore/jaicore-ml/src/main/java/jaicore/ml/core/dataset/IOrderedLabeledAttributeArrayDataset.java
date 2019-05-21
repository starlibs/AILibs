package jaicore.ml.core.dataset;

/**
 * Extends the {@link}IDataset by including the List interface.
 */
public interface IOrderedLabeledAttributeArrayDataset<I extends ILabeledAttributeArrayInstance> extends AILabeledAttributeArrayDataset<I>, IOrderedDataset<I> {

}