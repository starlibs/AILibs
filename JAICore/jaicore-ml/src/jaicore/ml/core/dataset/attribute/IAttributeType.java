package jaicore.ml.core.dataset.attribute;

/**
 * Wrapper interface for attribute types.
 *
 * @author wever
 *
 *         <D> domain of the attribute type.
 *
 */
public interface IAttributeType<D> {

	/**
	 * Validates whether a value conforms to this type.
	 *
	 * @param value
	 *            The value to validated.
	 * @return Returns true if the given value conforms
	 */
	public boolean isValidValue(D value);

	/**
	 * Casts the value to the respective type and returns an attribute value with the creating attribute type as the referenced type.
	 *
	 * @param value
	 *            The value of the attribute.
	 * @return An attribute value object holding the value of the attribute and referring to this attribute type.
	 */
	public IAttributeValue<D> buildAttributeValue(Object value);

	/**
	 * Builds an attribute value object from a string description. The attribute value references this attribute type.
	 *
	 * @param stringDescription
	 *            A String-format description of the attribute's value.
	 * @return The attribute value object holding an attribute value interpreting the string description and referring to this attribute type.
	 */
	public IAttributeValue<D> buildAttributeValue(String stringDescription);

}
