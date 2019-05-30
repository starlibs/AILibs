package jaicore.ml.core.dataset;

public interface ILabeledAttributeArrayDataset<L> extends AILabeledAttributeArrayDataset<ILabeledAttributeArrayInstance<L>, L> {
	
	/**
	 * Creates an empty copy with the same attribute types as this IDataset.
	 *
	 * @return The newly created Dataset.
	 */
	public ILabeledAttributeArrayDataset<L> createEmpty();
}
