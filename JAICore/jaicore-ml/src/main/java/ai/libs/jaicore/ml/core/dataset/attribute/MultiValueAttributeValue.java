package ai.libs.jaicore.ml.core.dataset.attribute;

import java.util.Collection;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IMultiLabelAttributeValue;

public class MultiValueAttributeValue implements IMultiLabelAttributeValue {

	private MultiValueAttributeType attribute;
	private Collection<String> value;

	public MultiValueAttributeValue(final MultiValueAttributeType attribute, final Collection<String> value) {
		this.attribute = attribute;
		this.value = value;
	}

	@Override
	public Collection<String> getValue() {
		return this.value;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

}
