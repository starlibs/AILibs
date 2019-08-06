package ai.libs.jaicore.ml.core.dataset.attribute;

import org.api4.java.ai.ml.dataset.attribute.IAttributeValue;

public class AAttributeValue<O> implements IAttributeValue<O> {

	private O value;

	protected AAttributeValue(final O value) {
		this.value = value;
	}

	@Override
	public O get() {
		return this.value;
	}

}
