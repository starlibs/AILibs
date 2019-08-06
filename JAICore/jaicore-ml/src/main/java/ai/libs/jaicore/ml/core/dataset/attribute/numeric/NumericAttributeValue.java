package ai.libs.jaicore.ml.core.dataset.attribute.numeric;

import org.api4.java.ai.ml.dataset.attribute.numeric.INumericAttributeValue;

import ai.libs.jaicore.ml.core.dataset.attribute.AAttributeValue;

public class NumericAttributeValue extends AAttributeValue<Double> implements INumericAttributeValue {

	public NumericAttributeValue(final double value) {
		super(value);
	}

}
