package jaicore.ml.core.dataset.attribute.primitive;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The numeric attribute type.
 *
 * @author wever
 *
 */
public class NumericAttributeType implements IPrimitiveAttributeType<Double> {

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

}
