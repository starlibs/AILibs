package jaicore.ml.core.dataset.attribute.primitive;

import jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Numeric attribute value as it can be part of an instance.
 *
 * @author wever
 */
public class NumericAttributeValue extends AAttributeValue<Double> {

	/**
	 * Standard c'tor.
	 *
	 * @param type
	 *            The type defining the domain of this numeric attribute.
	 */
	public NumericAttributeValue(final NumericAttributeType type) {
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
	public NumericAttributeValue(final NumericAttributeType type, final Double value) {
		super(type, value);
	}

}
