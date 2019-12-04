package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

public class IntBasedCategoricalAttributeValue implements ICategoricalAttributeValue {

	private final ICategoricalAttribute attribute;
	private final int value;

	public IntBasedCategoricalAttributeValue(final ICategoricalAttribute attribute, final int value) {
		this.attribute = attribute;
		this.value = value;
	}

	public IntBasedCategoricalAttributeValue(final ICategoricalAttributeValue value) {
		this(value.getAttribute(), value.getValue());
	}

	@Override
	public Integer getValue() {
		return this.value;
	}

	@Override
	public ICategoricalAttribute getAttribute() {
		return this.attribute;
	}

}
