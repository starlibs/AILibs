package jaicore.ml.core.dataset;

public interface ILabeledAttributeArrayDataset extends AILabeledAttributeArrayDataset<ILabeledAttributeArrayInstance> {
	
	/**
	 * Creates an empty copy with the same attribute types as this IDataset.
	 *
	 * @return The newly created Dataset.
	 */
	public ILabeledAttributeArrayDataset createEmpty();
}
