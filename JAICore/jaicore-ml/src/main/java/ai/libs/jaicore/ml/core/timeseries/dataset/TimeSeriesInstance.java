package ai.libs.jaicore.ml.core.timeseries.dataset;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.timeseries.model.INDArrayTimeseries;

/**
 * TimeSeriesInstance
 */
public class TimeSeriesInstance<L> implements ILabeledInstance {

	/** Attribute values of the instance. */
	private INDArrayTimeseries[] attributeValues;

	/** Target value of the instance. */
	private L targetValue;

	/**
	 * Constructor.
	 *
	 * @param dataset
	 * @param attributeValues
	 * @param targetValue
	 */
	public TimeSeriesInstance(final INDArrayTimeseries[] attributeValues, final L targetValue) {
		// Set attributes.
		this.attributeValues = attributeValues;
		this.targetValue = targetValue;
	}

	public TimeSeriesInstance(final List<INDArrayTimeseries> attributeValues, final L targetValue) {
		int n = attributeValues.size();
		this.attributeValues = new INDArrayTimeseries[n];
		for (int i = 0; i < n; i++) {
			this.attributeValues[i] = attributeValues.get(i);
		}
		this.targetValue = targetValue;
	}

	@Override
	public INDArrayTimeseries getAttributeValue(final int pos) {
		return this.attributeValues[pos];
	}

	@Override
	public L getLabel() {
		return this.targetValue;
	}

	@Override
	public Iterator<INDArrayTimeseries> iterator() {
		return Arrays.stream(this.attributeValues).iterator();
	}

	@Override
	public int getNumAttributes() {
		return this.attributeValues.length;
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
	public double getPointValue(final int pos) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		// TODO Auto-generated method stub
		return null;
	}
}