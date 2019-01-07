package jaicore.ml.core.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset implements IDataset<TimeSeriesInstance> {

	/** Number of instances contained in the dataset. */
	private long numberOfInstances;

	/** Values of time series variables. */
	private List<INDArray> valueMatrices;

	/** Timestamps of time series variables. */
	private List<INDArray> timestampMatrices;

	/** Target values for the instances. */
	private INDArray targets;

	/**
	 * Attribute types for the time series variables contained in this dataset.
	 * These are implicitly created/removed whenever a new timeseries variable (as
	 * INDArray-matrix) is added to/removed from the dataset. Used to be able to
	 * reconstruct {@link}TimeSeriesInstances from the stored matrices.
	 */
	private List<IAttributeType<?>> attributeTypes;

	private IAttributeType<?> targetType;

	/**
	 * Creates a TimeSeries dataset. Let `n` be the number of instances.
	 * 
	 * @param valueMatrices     Values for the time series variables. List of
	 *                          2D-Arrays with shape `[n, ?]`.
	 * @param timestampMatrices Timestamps for the time series variables. List of
	 *                          2D-Arrays with shape `[n, ?]`. Or `null` if no
	 *                          timestamps exist for the corresponding time series
	 *                          variable. The shape of the `i`th index must be equal
	 *                          to the shape of the `i`th element of
	 *                          `valueMatrices`.
	 * @param targets           Target values for the instances.
	 */
	public TimeSeriesDataset(List<INDArray> valueMatrices, List<INDArray> timestampMatrices, INDArray targets) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).shape()[0];
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
		// Create time series attributes types.
		attributeTypes = new ArrayList<>();
		for (INDArray valueMatrix : valueMatrices) {
			createAttributeType(valueMatrix);
		}
		// Create target attribute type.
		targetType = new NumericAttributeType();
	}

	/**
	 * Add a time series variable to the dataset.
	 * 
	 * @param valueMatrix     Values for the time series variable to add. 2D-Arrays
	 *                        with shape `[n, ?]` where `n` is the number of
	 *                        instances of the dataset.
	 * @param timestampMatrix Timestamps for the time series variable to add.
	 *                        2D-Arrays with shape `[n, ?]` where `n` is the number
	 *                        of instances of the dataset. Or `null` if no timestamp
	 *                        exists for this time series variable.
	 */
	public void add(INDArray valueMatrix, INDArray timestampMatrix) {
		// Parameter checks.
		// ..
		valueMatrices.add(valueMatrix);
		timestampMatrices.add(timestampMatrix);
		addAttributeType(valueMatrix);
	}

	/**
	 * Removes the time series variable at a given index.
	 * 
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(int index) throws IndexOutOfBoundsException {
		valueMatrices.remove(index);
		timestampMatrices.remove(index);
		attributeTypes.remove(index);
	}

	/**
	 * Replaces the time series variable at a given index with a new one.
	 * 
	 * @param index           Index of the time series varialbe to replace.
	 * @param valueMatrix     Values for the time series variable to add. 2D-Arrays
	 *                        with shape `[n, ?]` where `n` is the number of
	 *                        instances of the dataset.
	 * @param timestampMatrix Timestamps for the time series variable to add.
	 *                        2D-Arrays with shape `[n, ?]` where `n` is the number
	 *                        of instances of the dataset. Or `null` if no timestamp
	 *                        exists for this time series variable.
	 * @throws IndexOutOfBoundsException Thrown if `numberOfInstances <= index`.
	 */
	public void replace(int index, INDArray valueMatrix, INDArray timestampMatrix) throws IndexOutOfBoundsException {
		valueMatrices.set(index, valueMatrix);
		timestampMatrices.set(index, timestampMatrix);
		TimeSeriesAttributeType type = createAttributeType(valueMatrix);
		attributeTypes.set(index, type);
	}

	public INDArray getTargets() {
		return targets;
	}

	public int getNumberOfVariables() {
		return valueMatrices.size();
	}

	public long getNumberOfInstances() {
		return numberOfInstances;
	}

	public INDArray getValues(int index) throws IndexOutOfBoundsException {
		return valueMatrices.get(index);
	}

	public INDArray getTimestamps(int index) throws IndexOutOfBoundsException {
		return timestampMatrices.get(index);
	}

	public INDArray getValuesOrNull(int index) {
		return valueMatrices.size() < index ? valueMatrices.get(index) : null;
	}

	public INDArray getTimestampsOrNull(int index) {
		return timestampMatrices.size() < index ? timestampMatrices.get(index) : null;
	}

	public boolean isEmpty() {
		return valueMatrices.size() == 0;
	}

	public boolean isUnivariate() {
		return valueMatrices.size() == 1;
	}

	public boolean isMultivariate() {
		return valueMatrices.size() > 1;
	}

	// --
	// Intern helper functions.
	// --

	private TimeSeriesAttributeType createAttributeType(INDArray valueMatrix) {
		int length = (int) valueMatrix.shape()[1];
		TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
		return type;
	}

	private void addAttributeType(INDArray valueMatrix) {
		TimeSeriesAttributeType type = createAttributeType(valueMatrix);
		attributeTypes.add(type);
	}

	// --
	// IDataset interface.
	// --

	/**
	 * Iterator for the @{@link}TimeSeriesDataset. Iterates and implicitly creates
	 * the @{link}TimeSeriesInstance.
	 */
	class TimeSeriesDatasetIterator implements Iterator<TimeSeriesInstance> {

		int current = 0;

		@Override
		public boolean hasNext() {
			return TimeSeriesDataset.this.numberOfInstances > current ? true : false;
		}

		@Override
		public TimeSeriesInstance next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			// Build attribute value as view on the row of the attribute matrix.
			List<IAttributeValue<?>> attributeValues = new ArrayList<>();
			for (int i = 0; i < TimeSeriesDataset.this.valueMatrices.size(); i++) {
				INDArray viewOnCurrent = TimeSeriesDataset.this.valueMatrices.get(i).getRow(current);
				IAttributeType<?> type = TimeSeriesDataset.this.attributeTypes.get(i);
				IAttributeValue<?> value = type.buildAttributeValue(viewOnCurrent);
				attributeValues.add(value);
			}
			// Build target value.
			double target = TimeSeriesDataset.this.targets.getDouble(current);
			IAttributeValue<?> targetValue = TimeSeriesDataset.this.targetType.buildAttributeValue(target);
			// Return time series instance.
			current++;
			TimeSeriesInstance instance = new TimeSeriesInstance(attributeValues, targetValue);
			return instance;
		}

	}

	@Override
	public Iterator<TimeSeriesInstance> iterator() {
		return new TimeSeriesDatasetIterator();
	}

	@Override
	public int getNumberOfAttributes() {
		return attributeTypes.size();
	}

	@Override
	public void serialize(OutputStream out) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deserialize(InputStream in) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> IAttributeType<T> getTargetType(Class<? extends T> clazz) {
		return (IAttributeType<T>) targetType;
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		return attributeTypes;
	}

}
