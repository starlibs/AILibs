package jaicore.ml.core.dataset;

import org.apache.commons.math3.ml.clustering.Clusterable;

import jaicore.ml.core.dataset.attribute.IAttributeValue;

public interface INumericArrayInstance extends IAttributeArrayInstance, Clusterable {
	public IAttributeValue<Double> getAttributeValue(int position);

	/**
	 * Turns the instance into a double vector.
	 *
	 * @return The instance as a double vector.
	 *
	 * @throws ContainsNonNumericAttributesException
	 *             Thrown if the instance is to be converted into a double vector and still contains non-numeric attributes.
	 */
	public double[] getAsDoubleVector() throws ContainsNonNumericAttributesException;

	@Override
	default double[] getPoint() {
		try {
			return getAsDoubleVector();
		} catch (ContainsNonNumericAttributesException e) {
			throw new UnsupportedOperationException(e);
		}
	}
}
