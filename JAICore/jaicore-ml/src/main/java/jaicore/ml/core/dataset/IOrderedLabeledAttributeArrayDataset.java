package jaicore.ml.core.dataset;

/**
 * Extends the {@link}IDataset by including the List interface.
 */
public interface IOrderedLabeledAttributeArrayDataset<I extends ILabeledAttributeArrayInstance<L>, L> extends AILabeledAttributeArrayDataset<I, L>, IOrderedDataset<I> {

}