package ai.libs.jaicore.ml.core.timeseries.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.core.timeseries.dataset.attribute.NDArrayTimeseriesAttribute;
import ai.libs.jaicore.ml.core.timeseries.model.INDArrayTimeseries;
import ai.libs.jaicore.ml.core.timeseries.model.NDArrayTimeseries;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset<L> implements ILabeledDataset<TimeSeriesInstance<L>> {

	/** Number of instances contained in the dataset. */
	private long numberOfInstances;

	/** Values of time series variables. */
	private List<INDArray> valueMatrices;

	/** Timestamps of time series variables. */
	private List<INDArray> timestampMatrices;

	/** Target values for the instances. */
	private List<L> targets;

	private final List<IAttribute> targetTypes;

	/**
	 * Attribute types for the time series variables contained in this dataset.
	 * These are implicitly created/removed whenever a new timeseries variable (as
	 * INDArray-matrix) is added to/removed from the dataset. Used to be able to
	 * reconstruct {@link}TimeSeriesInstances from the stored matrices.
	 */
	private List<IAttribute> attributeTypes;

	/**
	 * Creates a TimeSeries dataset. Let `n` be the number of instances.
	 *
	 * @param valueMatrices Values for the time series variables. List of
	 *            2D-Arrays with shape `[n, ?]`.
	 * @param timestampMatrices Timestamps for the time series variables. List of
	 *            2D-Arrays with shape `[n, ?]`. Or `null` if no
	 *            timestamps exist for the corresponding time series
	 *            variable. The shape of the `i`th index must be equal
	 *            to the shape of the `i`th element of
	 *            `valueMatrices`.
	 * @param targets Target values for the instances.
	 */
	public TimeSeriesDataset(final List<INDArray> valueMatrices, final List<INDArray> timestampMatrices, final List<L> targets, final List<IAttribute> targetType) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).shape()[0];
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
		// Create time series attributes types.
		this.attributeTypes = new ArrayList<>();
		int i = 0;
		for (INDArray valueMatrix : valueMatrices) {
			this.addAttributeType("ts" + (i++), valueMatrix);
		}
		// Create target attribute type.
		this.targetTypes = targetType;
	}

	/**
	 * Add a time series variable to the dataset.
	 *
	 * @param valueMatrix Values for the time series variable to add. 2D-Arrays
	 *            with shape `[n, ?]` where `n` is the number of
	 *            instances of the dataset.
	 * @param timestampMatrix Timestamps for the time series variable to add.
	 *            2D-Arrays with shape `[n, ?]` where `n` is the number
	 *            of instances of the dataset. Or `null` if no timestamp
	 *            exists for this time series variable.
	 */
	public void add(final String attributeName, final INDArray valueMatrix, final INDArray timestampMatrix) {
		// Parameter checks.
		// ..
		this.valueMatrices.add(valueMatrix);
		this.timestampMatrices.add(timestampMatrix);
		this.addAttributeType(attributeName, valueMatrix);
	}

	/**
	 * Removes the time series variable at a given index.
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public TimeSeriesInstance<L> remove(final int index) {
		TimeSeriesInstance<L> instance = this.get(index);
		this.valueMatrices.remove(index);
		this.timestampMatrices.remove(index);
		this.attributeTypes.remove(index);
		return instance;
	}

	/**
	 * Replaces the time series variable at a given index with a new one.
	 *
	 * @param index Index of the time series variable to replace.
	 * @param valueMatrix Values for the time series variable to add. 2D-Arrays
	 *            with shape `[n, ?]` where `n` is the number of
	 *            instances of the dataset.
	 * @param timestampMatrix Timestamps for the time series variable to add.
	 *            2D-Arrays with shape `[n, ?]` where `n` is the number
	 *            of instances of the dataset. Or `null` if no timestamp
	 *            exists for this time series variable.
	 * @throws IndexOutOfBoundsException Thrown if `numberOfInstances <= index`.
	 */
	public void replace(final int index, final INDArray valueMatrix, final INDArray timestampMatrix) {
		this.valueMatrices.set(index, valueMatrix);
		if (timestampMatrix != null && this.timestampMatrices != null && this.timestampMatrices.size() > index) {
			this.timestampMatrices.set(index, timestampMatrix);
		}
		NDArrayTimeseriesAttribute type = this.createAttributeType("ts" + index, valueMatrix);
		this.attributeTypes.set(index, type);
	}

	public List<L> getTargets() {
		return this.targets;
	}

	public INDArray getTargetsAsINDArray() {
		if (this.targets.get(0) instanceof Number) {
			return Nd4j.create(this.targets.stream().mapToDouble(x -> (Double) x).toArray());
		}
		return null;
	}

	public int getNumberOfVariables() {
		return this.valueMatrices.size();
	}

	public long getNumberOfInstances() {
		return this.numberOfInstances;
	}

	public INDArray getValues(final int index) {
		return this.valueMatrices.get(index);
	}

	public INDArray getTimestamps(final int index) {
		return this.timestampMatrices.get(index);
	}

	public INDArray getValuesOrNull(final int index) {
		return this.valueMatrices.size() > index ? this.valueMatrices.get(index) : null;
	}

	public INDArray getTimestampsOrNull(final int index) {
		return this.timestampMatrices != null && this.timestampMatrices.size() > index ? this.timestampMatrices.get(index) : null;
	}

	@Override
	public boolean isEmpty() {
		return this.valueMatrices.isEmpty();
	}

	public boolean isUnivariate() {
		return this.valueMatrices.size() == 1;
	}

	public boolean isMultivariate() {
		return this.valueMatrices.size() > 1;
	}

	// --
	// Intern helper functions.
	// --

	private NDArrayTimeseriesAttribute createAttributeType(final String name, final INDArray valueMatrix) {
		int length = (int) valueMatrix.shape()[1];
		return new NDArrayTimeseriesAttribute(name, length);
	}

	private void addAttributeType(final String name, final INDArray valueMatrix) {
		NDArrayTimeseriesAttribute type = this.createAttributeType(name, valueMatrix);
		this.attributeTypes.add(type);
	}

	// --
	// IDataset interface.
	// --

	/**
	 * Iterator for the @{@link}TimeSeriesDataset. Iterates and implicitly creates
	 * the @{link}TimeSeriesInstance.
	 */
	class TimeSeriesDatasetIterator implements Iterator<TimeSeriesInstance<L>> {

		private int current = 0;

		@Override
		public boolean hasNext() {
			return TimeSeriesDataset.this.numberOfInstances > this.current;
		}

		@Override
		public TimeSeriesInstance<L> next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			return TimeSeriesDataset.this.get(this.current++);
		}
	}

	@Override
	public TimeSeriesInstance<L> get(final int index) {

		// Build attribute value as view on the row of the attribute matrix.
		List<INDArrayTimeseries> attributeValues = new ArrayList<>();
		for (int i = 0; i < TimeSeriesDataset.this.valueMatrices.size(); i++) {
			attributeValues.add(new NDArrayTimeseries(TimeSeriesDataset.this.valueMatrices.get(i).getRow(index)));
		}
		// Build target value.
		L target = TimeSeriesDataset.this.targets.get(index);
		return new TimeSeriesInstance<>(attributeValues, target);
	}

	@Override
	public Iterator<TimeSeriesInstance<L>> iterator() {
		return new TimeSeriesDatasetIterator();
	}

	@Override
	public boolean add(final TimeSeriesInstance<L> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends TimeSeriesInstance<L>> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(final int arg0, final TimeSeriesInstance<L> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends TimeSeriesInstance<L>> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(final Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(final Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<TimeSeriesInstance<L>> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<TimeSeriesInstance<L>> listIterator(final int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TimeSeriesInstance<L> set(final int arg0, final TimeSeriesInstance<L> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TimeSeriesInstance<L>> subList(final int arg0, final int arg1) {
		throw new UnsupportedOperationException();
	}

	public int getFrequency(final TimeSeriesInstance<L> instance) {
		return (int) this.stream().filter(instance::equals).count();
	}

	@Override
	public ILabeledInstanceSchema getInstanceSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[][] getFeatureMatrix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] getLabelVector() {
		// TODO Auto-generated method stub
		return null;
	}

}