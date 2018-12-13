package jaicore.ml.core.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeType;

/**
 * Common interface of a dataset defining methods to access meta-data and instances contained in the dataset. Moreover, methods for serialization and deserialization are defined.
 *
 * @author wever
 */
public interface IDataset extends List<IInstance> {

	/**
	 * Returns the attribute type of the target attribute.
	 *
	 * @return The attribute type of the target attribute.
	 */
	public <T> IAttributeType<T> getTargetType(Class<? extends T> clazz);

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
	 * Serializes the dataset and writes the serialized representation to the output stream.
	 *
	 * @param out
	 *            The output stream to which the dataset is serialized to.
	 */
	public void serialize(OutputStream out);

	/**
	 * Reads the data set from the provided input stream.
	 *
	 * @param in
	 *            The input stream providing the data of the data set to be deserialized.
	 */
	public void deserialize(InputStream in);

}
