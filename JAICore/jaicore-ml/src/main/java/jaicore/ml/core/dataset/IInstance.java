package jaicore.ml.core.dataset;

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
	 * @param position
	 *            The position of the attribute within the instance.
	 * @param type
	 *            The type for which the attribute value shall be returned.
	 * @return The attribute value for the position.
	 */
	public <T> IAttributeValue<T> getAttributeValue(int position, Class<T> type);

	/**
	 * Getter for the value of the target attribute.
	 *
	 * @param type
	 *            The type to bind the value of the target attribute.
	 * @return The value of the traget attribute.
	 */
	public <T> IAttributeValue<T> getTargetValue(Class<T> type);

	/**
	 * Turns the instance into a double vector.
	 *
	 * @return The instance as a double vector.
	 *
	 * @throws ContainsNonNumericAttributesException
	 *             Thrown if the instance is to be converted into a double vector and still contains non-numeric attributes.
	 */
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException;

}
