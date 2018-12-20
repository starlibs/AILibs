package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Common interface of a dataset defining methods to access meta-data and instances contained in the dataset. Moreover, methods for serialization and deserialization are defined.
 *
 * @author wever
 * @author jnowack
 */
public interface IDataset<I extends IInstance> extends List<I> {

	/**
	 * Returns the attribute type of the target attribute.
	 *
	 * @return The attribute type of the target attribute.
	 */
	public <T> IAttributeType<T> getTargetType(Class<T> clazz);

	/**
	 * @return Returns the plain attribute type of the target.
	 */
	public IAttributeType<?> getTargetType();

	/**
	 * Returns the list of attribute types.
	 *
	 * @return The list of attribute types.
	 */
	public List<IAttributeType<?>> getAttributeTypes();

	/**
	 * Getter for the number of attributes (excluding target attribute).
	 *
	 * @return The number of attributes.
	 */
	public int getNumberOfAttributes();

	
	/**
	 * Creates an empty copy with the same attribute types as this IDataset.
	 * 
	 * @return The newly created Dataset.
	 */
	public IDataset<I> createEmpty();
	
}
