package jaicore.ml.core.dataset;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Interface of an instance that consists of attributes.
 *
 * @author wever, fmohr
 *
 */
public interface IAttributeArrayInstance {
	
	public IAttributeValue<?>[] getAllAttributeValues();
	
	/**
	 * Getter for the value of an attribute for the given position.
	 *
	 * @param position The position of the attribute within the instance.
	 * @param type     The type for which the attribute value shall be returned.
	 * @return The attribute value for the position.
	 */
	public <T> IAttributeValue<T> getAttributeValueAtPosition(int position, Class<T> type);

	/**
	 * Getter for the number of attributes for the instance.
	 *
	 * @return Number of attributes
	 */
	public int getNumberOfAttributes();
}
