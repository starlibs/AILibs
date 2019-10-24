package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ICategoricalAttributeValue;

public class CategoricalAttributeValue implements ICategoricalAttributeValue {

	private final ICategoricalAttribute attribute;
	private final String value;

	public CategoricalAttributeValue(final ICategoricalAttribute attribute, final String value) {
		this.attribute = attribute;
		this.value = value;
	}

	public CategoricalAttributeValue(final ICategoricalAttributeValue value) {
		this(value.getAttribute(), value.getValue());
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public ICategoricalAttribute getAttribute() {
		return this.attribute;
	}

}
