package ai.libs.jaicore.ml.core.dataset.attribute.numeric;

import org.api4.java.ai.ml.dataset.attribute.numeric.INumericAttributeType;

import ai.libs.jaicore.ml.core.dataset.attribute.AAttributeType;

public class NumericAttributeType extends AAttributeType implements INumericAttributeType {

	/**
	 *
	 */
	private static final long serialVersionUID = 657993241775006166L;

	public NumericAttributeType(final String name) {
		super(name);
	}

	@Override
	public boolean isValidValue(final Object value) {
		if (value instanceof Number) {
			return true;
		}
		return false;
	}

	@Override
	public NumericAttributeValue buildAttributeValue(final Object value) {
		if (!this.isValidValue(value)) {
			throw new IllegalArgumentException("No valid value of this numeric attribute");
		}
		return new NumericAttributeValue(Double.parseDouble(value + ""));
	}

	@Override
	public String getStringDescriptionOfDomain() {
		return "[Num] " + this.getName();
	}

}
