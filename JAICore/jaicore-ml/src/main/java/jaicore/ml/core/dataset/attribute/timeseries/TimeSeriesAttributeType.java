package jaicore.ml.core.dataset.attribute.timeseries;

import java.util.stream.Stream;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;

/**
 * Describes a time series type as an 1-NDArray with a fixed length.
 */
public class TimeSeriesAttributeType implements IAttributeType<INDArray> {

	private int length;

	public TimeSeriesAttributeType(final int length) {
		this.length = length;
	}

	/**
	 * Validates whether a INDArray conforms to this time series. An INDArray
	 * confirms to this value, if its rank is 1 and its length equals the length of
	 * this time series.
	 *
	 * @param value The value to validated.
	 * @return Returns true if the given value conforms
	 */
	@Override
	public boolean isValidValue(final INDArray value) {
		return value.rank() == 1 && value.length() == this.length;
	}

	@Override
	public IAttributeValue<INDArray> buildAttributeValue(final Object value) {
		if (value instanceof INDArray) {
			return new TimeSeriesAttributeValue(this, (INDArray) value);
		} else {
			throw new IllegalArgumentException("Value has to be an INDArray");
		}
	}

	@Override
	public IAttributeValue<INDArray> buildAttributeValue(final String stringDescription) {
		double[] data = Stream.of(stringDescription.split(",")).mapToDouble(Double::parseDouble).toArray();
		int[] shape = { data.length };
		INDArray value = Nd4j.create(data, shape);
		return this.buildAttributeValue(value);
	}

	/**
	 * Get the length of this time series attribute type.
	 *
	 * @return The length respec. the number of datapoints of this time series
	 *         attribute.
	 */
	public int getLength() {
		return this.length;
	}

	public void setLength(final int length) {
		if (length < 0) {
			throw new IllegalArgumentException("the length has to be greater than or equal to zero.");
		}
		this.length = length;
	}

}