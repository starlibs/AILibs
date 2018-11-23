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

}
