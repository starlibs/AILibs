package jaicore.ml.core.dataset.attribute.primitive;

import jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Numeric attribute value as it can be part of an instance.
 *
 * @author wever
 */
public class BooleanAttributeValue extends AAttributeValue<Boolean> {

	/**
	 * Standard c'tor.
	 *
	 * @param type
	 *            The type defining the domain of this numeric attribute.
	 */
	public BooleanAttributeValue(final BooleanAttributeType type) {
		super(type);
	}

	/**
	 * C'tor setting the value of this attribute as well.
	 *
	 * @param type
	 *            The type defining the domain of this numeric attribute.
	 * @param value
	 *            The value this attribute takes.
	 */
	public BooleanAttributeValue(final BooleanAttributeType type, final Boolean value) {
		super(type, value);
	}

}
