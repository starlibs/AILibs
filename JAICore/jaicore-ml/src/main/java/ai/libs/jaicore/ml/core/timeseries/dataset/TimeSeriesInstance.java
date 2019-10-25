package ai.libs.jaicore.ml.core.timeseries.dataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ai.libs.jaicore.ml.core.timeseries.model.INDArrayTimeseries;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance implements ITimeSeriesInstance {

	/** Attribute values of the instance. */
	private List<INDArrayTimeseries> attributeValues;

	/** Target value of the instance. */
	private Object targetValue;

	/**
	 * Constructor.
	 *
	 * @param dataset
	 * @param attributeValues
	 * @param targetValue
	 */
	public TimeSeriesInstance(final INDArrayTimeseries[] attributeValues, final L targetValue) {
		// Set attributes.
		this.attributeValues = Arrays.asList(attributeValues);
		this.targetValue = targetValue;
	}

	public TimeSeriesInstance(final List<INDArrayTimeseries> attributeValues, final L targetValue) {
		int n = attributeValues.size();
		this.attributeValues = attributeValues;
		this.targetValue = targetValue;
	}

	@Override
	public INDArrayTimeseries getAttributeValue(final int pos) {
		return this.attributeValues.get(pos);
	}

	@Override
	public Object getLabel() {
		return this.targetValue;
	}

	@Override
	public Iterator<INDArrayTimeseries> iterator() {
		return Arrays.stream(this.attributeValues).iterator();
	}

	public INDArrayTimeseries[] getAllFeatures() {
		return this.attributeValues;
	}

	@Override
	public double[] getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeColumn(final int columnPos) {

	}

	@Override
	public double getPointValue(final int pos) {
		// TODO Auto-generated method stub
		return 0;
	}

}