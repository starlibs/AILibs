package ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.model.INDArrayTimeseries;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance implements ITimeSeriesInstance {

	/** Attribute values of the instance. */
	private List<INDArrayTimeseries> attributeValues;

	/** Target value of the instance. */
	private Object label;

	/**
	 * Constructor.
	 *
	 * @param dataset
	 * @param attributeValues
	 * @param targetValue
	 */
	public TimeSeriesInstance(final INDArrayTimeseries[] attributeValues, final Object targetValue) {
		this(Arrays.asList(attributeValues), targetValue);
	}

	public TimeSeriesInstance(final List<INDArrayTimeseries> attributeValues, final Object targetValue) {
		this.attributeValues = attributeValues;
		this.label = targetValue;
	}

	@Override
	public INDArrayTimeseries getAttributeValue(final int pos) {
		return this.attributeValues.get(pos);
	}

	@Override
	public Object getLabel() {
		return this.label;
	}

	@Override
	public Iterator<INDArrayTimeseries> iterator() {
		return this.attributeValues.iterator();
	}

	@Override
	public double[] getPoint() {
		double[] point = new double[this.attributeValues.stream().mapToInt(INDArrayTimeseries::length).sum()];

		int i = 0;
		for (INDArrayTimeseries series : this.attributeValues) {
			double[] seriesPoint = series.getPoint();
			for (int j = 0; j < seriesPoint.length; j++) {
				point[i++] = seriesPoint[j];
			}
		}
		return point;
	}

	@Override
	public Object[] getAttributes() {
		return this.attributeValues.toArray();
	}

	@Override
	public void removeColumn(final int columnPos) {
		if (columnPos < this.attributeValues.size() && columnPos >= 0) {
			this.attributeValues.remove(columnPos);
		} else {
			throw new IllegalArgumentException("The index is not valid.");
		}
	}

	@Override
	public double getPointValue(final int pos) {
		throw new UnsupportedOperationException("This operation is not supported.");
	}

	@Override
	public void setLabel(final Object label) {
		this.label = label;
	}

	@Override
	public void setAttributeValue(final int pos, final Object value) {
		if (!(value instanceof INDArrayTimeseries)) {
			throw new IllegalArgumentException("The given value is no timeseries.");
		}
		this.attributeValues.add((INDArrayTimeseries) value);
	}

	@Override
	public boolean isLabelPresent() {
		return this.label != null;
	}

}