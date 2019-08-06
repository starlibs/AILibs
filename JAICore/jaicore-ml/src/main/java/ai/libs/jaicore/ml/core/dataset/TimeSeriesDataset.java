package ai.libs.jaicore.ml.core.dataset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.api4.java.ai.ml.dataset.attribute.IAttributeType;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.core.dataset.attribute.timeseries.INDArrayTimeseries;
import ai.libs.jaicore.ml.core.dataset.attribute.timeseries.NDArrayTimeseries;
import ai.libs.jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset<Y> implements ISupervisedDataset<INDArrayTimeseries, Y, TimeSeriesInstance<Y>> {

	/** Number of instances contained in the dataset. */
	private long numberOfInstances;

	/** Values of time series variables. */
	private List<INDArray> valueMatrices;

	/** Timestamps of time series variables. */
	private List<INDArray> timestampMatrices;

	/** Target values for the instances. */
	private List<Y> targets;

	private final List<IAttributeType> targetTypes;

	/**
	 * Attribute types for the time series variables contained in this dataset.
	 * These are implicitly created/removed whenever a new timeseries variable (as
	 * INDArray-matrix) is added to/removed from the dataset. Used to be able to
	 * reconstruct {@link}TimeSeriesInstances from the stored matrices.
	 */
	private List<IAttributeType> attributeTypes;

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
	public TimeSeriesDataset(final List<INDArray> valueMatrices, final List<INDArray> timestampMatrices, final List<Y> targets, final List<IAttributeType> targetType) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).shape()[0];
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
		// Create time series attributes types.
		this.attributeTypes = new ArrayList<>();
		for (INDArray valueMatrix : valueMatrices) {
			this.addAttributeType(valueMatrix);
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
	public void add(final INDArray valueMatrix, final INDArray timestampMatrix) {
		// Parameter checks.
		// ..
		this.valueMatrices.add(valueMatrix);
		this.timestampMatrices.add(timestampMatrix);
		this.addAttributeType(valueMatrix);
	}

	/**
	 * Removes the time series variable at a given index.
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public TimeSeriesInstance<Y> remove(final int index) {
		TimeSeriesInstance<Y> instance = this.get(index);
		this.valueMatrices.remove(index);
		this.timestampMatrices.remove(index);
		this.attributeTypes.remove(index);
		return instance;
	}

	/**
	 * Replaces the time series variable at a given index with a new one.
	 *
	 * @param index Index of the time series varialbe to replace.
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
		TimeSeriesAttributeType type = this.createAttributeType(valueMatrix);
		this.attributeTypes.set(index, type);
	}

	public List<Y> getTargets() {
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

	private TimeSeriesAttributeType createAttributeType(final INDArray valueMatrix) {
		int length = (int) valueMatrix.shape()[1];
		return new TimeSeriesAttributeType(length);
	}

	private void addAttributeType(final INDArray valueMatrix) {
		TimeSeriesAttributeType type = this.createAttributeType(valueMatrix);
		this.attributeTypes.add(type);
	}

	// --
	// IDataset interface.
	// --

	/**
	 * Iterator for the @{@link}TimeSeriesDataset. Iterates and implicitly creates
	 * the @{link}TimeSeriesInstance.
	 */
	class TimeSeriesDatasetIterator implements Iterator<TimeSeriesInstance<Y>> {

		private int current = 0;

		@Override
		public boolean hasNext() {
			return TimeSeriesDataset.this.numberOfInstances > this.current;
		}

		@Override
		public TimeSeriesInstance<Y> next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			return TimeSeriesDataset.this.get(this.current++);
		}
	}

	@Override
	public TimeSeriesInstance<Y> get(final int index) {

		// Build attribute value as view on the row of the attribute matrix.
		List<INDArrayTimeseries> attributeValues = new ArrayList<>();
		for (int i = 0; i < TimeSeriesDataset.this.valueMatrices.size(); i++) {
			attributeValues.add(new NDArrayTimeseries(TimeSeriesDataset.this.valueMatrices.get(i).getRow(index)));
		}
		// Build target value.
		Y target = TimeSeriesDataset.this.targets.get(index);
		return new TimeSeriesInstance<>(attributeValues, target);
	}

	@Override
	public Iterator<TimeSeriesInstance<Y>> iterator() {
		return new TimeSeriesDatasetIterator();
	}

	@Override
	public int getNumFeatures() {
		return this.attributeTypes.size();
	}

	@Override
	public ISupervisedDataset<INDArrayTimeseries, Y, TimeSeriesInstance<Y>> createEmptyCopy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final TimeSeriesInstance<Y> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends TimeSeriesInstance<Y>> c) {
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
	public void add(final int arg0, final TimeSeriesInstance<Y> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final int arg0, final Collection<? extends TimeSeriesInstance<Y>> arg1) {
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
	public ListIterator<TimeSeriesInstance<Y>> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<TimeSeriesInstance<Y>> listIterator(final int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TimeSeriesInstance<Y> set(final int arg0, final TimeSeriesInstance<Y> arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TimeSeriesInstance<Y>> subList(final int arg0, final int arg1) {
		throw new UnsupportedOperationException();
	}

	public int getFrequency(final TimeSeriesInstance<Y> instance) {
		return (int) this.stream().filter(instance::equals).count();
	}

	@Override
	public List<IAttributeType> getLabelTypes() {
		return this.targetTypes;
	}

	@Override
	public int getNumLabels() {
		return this.targetTypes.size();
	}

	@Override
	public List<IAttributeType> getFeatureTypes() {
		return this.attributeTypes;
	}

}
