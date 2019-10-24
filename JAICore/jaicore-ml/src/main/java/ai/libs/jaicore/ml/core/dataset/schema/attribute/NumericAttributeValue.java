package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.INumericAttributeValue;

public class NumericAttributeValue implements INumericAttributeValue {

	private INumericAttribute attribute;
	private final double value;

	public NumericAttributeValue(final INumericAttribute attribute, final double value) {
		this.attribute = attribute;
		this.value = value;
	}

	public NumericAttributeValue(final INumericAttributeValue value) {
		this(value.getAttribute(), value.getValue());
	}

	@Override
	public Double getValue() {
		return this.value;
	}

	@Override
	public INumericAttribute getAttribute() {
		return this.attribute;
	}

}
