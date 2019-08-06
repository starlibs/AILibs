package ai.libs.jaicore.ml.core.dataset.attribute.nominal;

import org.api4.java.ai.ml.dataset.attribute.nominal.INominalAttributeValue;

import ai.libs.jaicore.ml.core.dataset.attribute.AAttributeValue;

public class NominalAttributeValue extends AAttributeValue<String> implements INominalAttributeValue {

	public NominalAttributeValue(final String value) {
		super(value);
	}

}
