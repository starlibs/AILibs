package jaicore.ml.core.dataset.attribute;

/**
 * A general interface for attribute values.
 *
 * @author wever
 *
 * @param <D>
 *            The type of an attribute value.
 */
public interface IAttributeValue<D> {

	/**
	 * @return The value of this attribute value.
	 */
	public D getValue();

	/**
	 * @param value
	 *            The value of this attribute value.
	 */
	public void setValue(D value);

}
