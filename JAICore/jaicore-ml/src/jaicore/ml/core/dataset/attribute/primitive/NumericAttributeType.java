package jaicore.ml.core.dataset.attribute.primitive;

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

}
