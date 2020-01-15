package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IStringAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IStringAttributeValue;

public class StringAttributeValue implements IStringAttributeValue {

	private IStringAttribute attribute;
	private final String value;

	public StringAttributeValue(final IStringAttribute attribute, final String value) {
		this.attribute = attribute;
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public IStringAttribute getAttribute() {
		return this.attribute;
	}

}
