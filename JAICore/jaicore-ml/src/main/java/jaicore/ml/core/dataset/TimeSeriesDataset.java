package jaicore.ml.core.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.categorical.CategoricalAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset implements AILabeledAttributeArrayDataset<TimeSeriesInstance> {

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
	public TimeSeriesDataset(final List<INDArray> valueMatrices, final List<INDArray> timestampMatrices, final INDArray targets) {
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
		this.targetType = new NumericAttributeType();
	}

	/**
	 * Creates a TimeSeries dataset. Let `n` be the number of instances.
	 *
	 * @param valueMatrices
	 *            Values for the time series variables. List of 2D-Arrays with shape
	 *            `[n, ?]`.
	 * @param timestampMatrices
	 *            Timestamps for the time series variables. List of 2D-Arrays with
	 *            shape `[n, ?]`. Or `null` if no timestamps exist for the
	 *            corresponding time series variable. The shape of the `i`th index
	 *            must be equal to the shape of the `i`th element of
	 *            `valueMatrices`.
	 * @param targets
	 *            Target values for the instances.
	 * @param classNamens
	 *            Ordered list of String objects containing the mapped class names
	 *            for the target values (target value 0 corresponds to first list
	 *            element, ....).
	 */
	public TimeSeriesDataset(final List<INDArray> valueMatrices, final List<INDArray> timestampMatrices, final INDArray targets,
			final List<String> classNames) {
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
		this.targetType = new CategoricalAttributeType(classNames);
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
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(final int index) throws IndexOutOfBoundsException {
		this.valueMatrices.remove(index);
		this.timestampMatrices.remove(index);
		this.attributeTypes.remove(index);
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
	public void replace(final int index, final INDArray valueMatrix, final INDArray timestampMatrix) throws IndexOutOfBoundsException {
		this.valueMatrices.set(index, valueMatrix);
		if (timestampMatrix != null && this.timestampMatrices != null && this.timestampMatrices.size() > index) {
			this.timestampMatrices.set(index, timestampMatrix);
		}
		TimeSeriesAttributeType type = this.createAttributeType(valueMatrix);
		this.attributeTypes.set(index, type);
	}

	public INDArray getTargets() {
		return this.targets;
	}

	public int getNumberOfVariables() {
		return this.valueMatrices.size();
	}

	public long getNumberOfInstances() {
		return this.numberOfInstances;
	}

	public INDArray getValues(final int index) throws IndexOutOfBoundsException {
		return this.valueMatrices.get(index);
	}

	public INDArray getTimestamps(final int index) throws IndexOutOfBoundsException {
		return this.timestampMatrices.get(index);
	}

	public INDArray getValuesOrNull(final int index) {
		return this.valueMatrices.size() > index ? this.valueMatrices.get(index) : null;
	}

	public INDArray getTimestampsOrNull(final int index) {
		return this.timestampMatrices != null && this.timestampMatrices.size() > index ? this.timestampMatrices.get(index) : null;
	}

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
		TimeSeriesAttributeType type = new TimeSeriesAttributeType(length);
		return type;
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
	class TimeSeriesDatasetIterator implements Iterator<TimeSeriesInstance> {

		int current = 0;

		@Override
		public boolean hasNext() {
			return TimeSeriesDataset.this.numberOfInstances > this.current ? true : false;
		}

		@Override
		public TimeSeriesInstance next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}
			// Build attribute value as view on the row of the attribute matrix.
			List<IAttributeValue<?>> attributeValues = new ArrayList<>();
			for (int i = 0; i < TimeSeriesDataset.this.valueMatrices.size(); i++) {
				INDArray viewOnCurrent = TimeSeriesDataset.this.valueMatrices.get(i).getRow(this.current);
				IAttributeType<?> type = TimeSeriesDataset.this.attributeTypes.get(i);
				IAttributeValue<?> value = type.buildAttributeValue(viewOnCurrent);
				attributeValues.add(value);
			}
			// Build target value.
			double target = TimeSeriesDataset.this.targets.getDouble(this.current);
			IAttributeValue<?> targetValue = TimeSeriesDataset.this.targetType.buildAttributeValue(target);
			// Return time series instance.
			this.current++;
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
		return this.attributeTypes.size();
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		return this.attributeTypes;
	}

	@Override
	public IAttributeType<?> getTargetType() {
		return this.targetType;
	}

	@Override
	public IDataset<TimeSeriesInstance> createEmpty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(TimeSeriesInstance e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends TimeSeriesInstance> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean contains(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}
}
