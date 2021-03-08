package ai.libs.jaicore.ml.core.dataset.schema.attribute;

import java.util.Arrays;

import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttributeValue;

public class MultidimensionalAttributeValue implements IAttributeValue {

	private final double[][] value;
	private final MultidimensionalAttribute attribute;

	public MultidimensionalAttributeValue(final MultidimensionalAttribute attribute, final double[][] value) {
		this.value = value;
		this.attribute = attribute;
	}

	@Override
	public IAttribute getAttribute() {
		return this.attribute;
	}

	@Override
	public double[][] getValue() {
		return this.value;
	}

	/**
	 * {@inheritDoc} two objects of class MultidimensionalAttributeValue are equal if and only if they have the same instance
	 * of MultidimensionalAttribute as attribute and their doulbe[][] value holds the same values(therefore the same content)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof MultidimensionalAttributeValue) {
			MultidimensionalAttributeValue compAttributeValue = (MultidimensionalAttributeValue) obj;
			return (this.attribute.equals(compAttributeValue.getAttribute()) && Arrays.deepEquals(this.value, compAttributeValue.getValue()));
		}
		throw new IllegalArgumentException("the given parameter obj is not of Type MultidimensionalAttributeValue");
	}

}
