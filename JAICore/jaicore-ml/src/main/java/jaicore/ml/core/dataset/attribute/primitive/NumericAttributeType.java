package jaicore.ml.core.dataset.attribute.primitive;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The numeric attribute type.
 *
 * @author wever
 *
 */
public class NumericAttributeType implements IPrimitiveAttributeType<Double> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6642799651483094864L;

	@Override
	public boolean isValidValue(final Double value) {
		return true;
	}

	@Override
	public IAttributeValue<Double> buildAttributeValue(final Object value) {
		return new NumericAttributeValue(this, (Double) value);
	}

	@Override
	public IAttributeValue<Double> buildAttributeValue(final String stringDescription) {
		return this.buildAttributeValue(Double.valueOf(stringDescription));
	}

	@Override
	public String toString() {
		return "NUM";
	}
}
