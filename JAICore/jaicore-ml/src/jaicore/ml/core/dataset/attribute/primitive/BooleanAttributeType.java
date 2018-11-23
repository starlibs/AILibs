package jaicore.ml.core.dataset.attribute.primitive;

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

}
