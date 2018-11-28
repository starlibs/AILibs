package jaicore.ml.core.dataset.attribute.primitive;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * The boolean attribute type.
 *
 * @author wever
 *
 */
public class BooleanAttributeType implements IPrimitiveAttributeType<Boolean> {

	@Override
	public boolean isValidValue(final Boolean value) {
		return true;
	}

	@Override
	public IAttributeValue<Boolean> buildAttributeValue(final Object value) {
		return new BooleanAttributeValue(this, (Boolean) value);
	}

	@Override
	public IAttributeValue<Boolean> buildAttributeValue(final String stringDescription) {
		return this.buildAttributeValue(Boolean.valueOf(stringDescription));
	}

}
