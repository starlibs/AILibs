package jaicore.ml.core.dataset;

import java.util.List;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Interface of an instance which consists of attributes and a target value.
 *
 * @author wever
 */
public interface IInstance {

	/**
	 * Getter for the value of an attribute for the given position.
	 *
	 * @param position The position of the attribute within the instance.
	 * @param type     The type for which the attribute value shall be returned.
	 * @return The attribute value for the position.
	 */
	public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type);

	/**
	 * Getter for the value of the target attribute.
	 *
	 * @param type The type to bind the value of the target attribute.
	 * @return The value of the traget attribute.
	 */
	public <T> IAttributeValue<T> getTargetValue(Class<T> type);

	/**
	 * Removes an attribute value for the given position.
	 * 
	 * @param position The position of the attribute within the instance.
	 * @return True, if the removal was successfull. False, otherwise.
	 */
	public boolean removeAttributeValue(int position);

	/**
	 * Getter for the number of attributes for the instance.
	 * 
	 * @return Number of attributes
	 */
	public int getNumberOfAttributes();

	/**
	 * Getter for the attribute values.
	 * 
	 * @return The attribute values of the instance.
	 */
	public List<IAttributeValue<?>> getAttributeValues();
}
