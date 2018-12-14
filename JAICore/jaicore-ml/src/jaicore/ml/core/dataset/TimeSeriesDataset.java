package jaicore.ml.core.dataset;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;

/**
 * Dataset that contains {@link TimeSeriesInstance}s. Fast operations on time
 * series attributes are supported through efficient usages of nd4j.INDArray s.
 */
public class TimeSeriesDataset implements IDataset<TimeSeriesInstance> {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 104391886027510853L;

	/** Capacity i.e. maximum number of instances the dataset can contain. */
	private final int capacity;

	/** Number of instances contained in the dataset. */
	private int numberOfInstances = 0;

	/** List of instances the dataset contains. */
	private final List<TimeSeriesInstance> instances;

	/** Attribute type of target variable. */
	private final IAttributeType<?> target;

	/** Attribute types. Does not contain target type. */
	private List<IAttributeType<?>> attributesTypes;

	/** Holds a matrix for every time series attribute within this dataset. */
	private HashMap<TimeSeriesAttributeType, INDArray> timeSeriesMatrices;

	/**
	 * Constructor.
	 * 
	 * @param capacity
	 *            Capacity i.e. maximum number of instances the dataset can contain.
	 * @param attributesTypes
	 *            Attribute type of target variable
	 * @param target
	 *            Attribute type of target variable.
	 */
	public TimeSeriesDataset(final int capacity, final List<IAttributeType<?>> attributesTypes,
			final IAttributeType<?> target) {
		this.capacity = capacity;
		this.instances = new ArrayList<>(capacity);
		this.attributesTypes = attributesTypes;
		this.target = target;
		this.timeSeriesMatrices = new HashMap<>();
		// Create a matrix for every time series attribute.
		for (IAttributeType<?> attributeType : attributesTypes) {
			if (attributeType instanceof TimeSeriesAttributeType) {
				TimeSeriesAttributeType tsAttributeType = (TimeSeriesAttributeType) attributeType;
				int length = tsAttributeType.getLength();
				INDArray matrix = Nd4j.create(capacity, length);
				timeSeriesMatrices.put(tsAttributeType, matrix);
			}
		}
	}

	public void removeAttributeType(IAttributeType<?> attributeType) {
		// TODO
		// Remove attribute for all instances.
		// Remove corresponding matrix for time series attributes.
	}

	// TODO: Very inefficent. Maybe rethink architecture of
	// datset/instances/attributes.
	/**
	 * Updates the Time Series Matrix for a given Time Series Attribute. The view of
	 * all instances on the old matrix is replaced to a corresponding view (same
	 * row) on the new matrix.
	 * 
	 * @param attributeType
	 *            Attribute type to update.
	 * @param matrix
	 *            The new matrix for the given attribute type.
	 */
	public void updateTimeSeriesMatrix(TimeSeriesAttributeType attributeType, INDArray matrix) {
		// Check parameter: Dimension of matrix.
		// Check parameter: Attribute type to update exists.
		int attributePosition = attributesTypes.indexOf(attributeType);
		if (attributePosition == -1)
			throw new IllegalArgumentException("The given attribute type is not an attribute type of this dataset.");
		// Update matrix.
		timeSeriesMatrices.put(attributeType, matrix);
		// Update views of instances.
		int instanceCounter = 0;
		for (TimeSeriesInstance instance : instances) {
			INDArray viewOnRow = matrix.getRow(instanceCounter);
			IAttributeValue<?> attributeValue = instance.getAttributeValues().get(attributePosition);
			// Check (maybe the Instances attributes have been modified).
			if (!(attributeValue instanceof TimeSeriesAttributeValue))
				throw new IllegalStateException("This attribute of instance at position " + instanceCounter
						+ "has no TimeSeriesAttributeValue at position " + attributePosition + ".");
			// Set the view on instance.
			TimeSeriesAttributeValue tsAttributeValue = (TimeSeriesAttributeValue) instance.getAttributeValues()
					.get(attributePosition);
			tsAttributeValue.setValue(viewOnRow);
			// Increment instance counter.
			instanceCounter++;
		}
	}

	/**
	 * Getter for the matrix of a specific time series attribute type.
	 * 
	 * @param attributeType
	 *            The time series attribute type to retrieve the matrix for.
	 * @return The matrix for the given attribute type.
	 */
	public INDArray getMatrixForAttributeType(TimeSeriesAttributeType attributeType) {
		INDArray matrix = timeSeriesMatrices.get(attributeType);
		if (matrix == null)
			throw new IllegalArgumentException(
					"There is no record for given time series attribute type in the dataset.");
		return matrix;
	}

	// IDataset

	@Override
	public <T> IAttributeType<T> getTargetType(Class<? extends T> clazz) {
		return (IAttributeType<T>) target;
	}

	@Override
	public List<IAttributeType<?>> getAttributeTypes() {
		return attributesTypes;
	}

	@Override
	public int getNumberOfAttributes() {
		return attributesTypes.size();
	}

	@Override
	public void serialize(OutputStream out) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public void deserialize(InputStream in) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	// List interface.

	/**
	 * Adds a time series instance to the dataset.
	 */
	@Override
	public boolean add(TimeSeriesInstance instance) {
		// Return false if the dataset is "full".
		if (numberOfInstances == capacity)
			return false;

		// Parameter checking: Number of attributes.
		if (instance.getNumberOfAttributes() != this.getNumberOfAttributes())
			throw new IllegalArgumentException("The number of attributes values of the instance ("
					+ instance.getNumberOfAttributes() + ") does not equal to the number of attributes of the dataset ("
					+ this.getNumberOfAttributes() + ").");
		// Parameter checking: Valid (equal) attribute types for attribute values.
		for (int i = 0; i < this.getAttributeTypes().size(); i++) {
			IAttributeType<?> attributeTypeFromDataset = this.getAttributeTypes().get(i);
			IAttributeType<?> attributeTypeFromParameter = instance.getAttributeValues().get(i).getType();
			if (attributeTypeFromDataset != attributeTypeFromParameter)
				throw new IllegalArgumentException(
						"The attribute type of the specified instances attribute value at position " + i
								+ "does not conform to the attribute type of the dataset at this position.");
		}

		// Append the time series value for every time series attribute of the given
		// time series instance to the corresponding matrix of the dataset.
		for (IAttributeValue<?> attributeValue : instance.getAttributeValues()) {
			if (attributeValue instanceof TimeSeriesAttributeValue) {
				TimeSeriesAttributeValue timeseriesAttributeValue = (TimeSeriesAttributeValue) attributeValue;
				INDArray attributeMatrix = timeSeriesMatrices.get(timeseriesAttributeValue.getType());
				// Append the time series value of the instance' attribute to to the matrix.
				attributeMatrix.putRow(numberOfInstances, timeseriesAttributeValue.getValue());
				// Set the value of the timeseries attribute value to a view on the newly
				// created row.
				INDArray row = attributeMatrix.getRow(numberOfInstances);
				timeseriesAttributeValue.setValue(row);
			}
		}
		instances.add(instance);
		numberOfInstances++;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends TimeSeriesInstance> isntances) {
		for (final TimeSeriesInstance instance : isntances)
			this.add(instance);
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends TimeSeriesInstance> c) {
		throw new UnsupportedOperationException("Adding at a specific index is not supported.");
	}

	@Override
	public void add(int index, TimeSeriesInstance element) {
		throw new UnsupportedOperationException("Adding at a specific index is not supported.");
	}

	@Override
	public int size() {
		return instances.size();
	}

	@Override
	public boolean isEmpty() {
		return instances.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return instances.contains(o);
	}

	@Override
	public Iterator<TimeSeriesInstance> iterator() {
		return instances.iterator();
	}

	@Override
	public Object[] toArray() {
		return instances.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return instances.toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return instances.containsAll(c);
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException(
				"Removing ob objects or instances is not supported. Create a new dataset insteand");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException(
				"Removing ob objects or instances is not supported. Create a new dataset instead.");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException(
				"Removing ob objects or instances is not supported. Create a new dataset instead.");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException(
				"Removing ob objects or instances is not supported. Create a new dataset instead.");
	}

	@Override
	public TimeSeriesInstance get(int index) {
		return instances.get(index);
	}

	@Override
	public TimeSeriesInstance set(int index, TimeSeriesInstance element) {
		throw new UnsupportedOperationException("Adding at a specific index is not supported.");
	}

	@Override
	public TimeSeriesInstance remove(int index) {
		throw new UnsupportedOperationException(
				"Removing ob objects or instances is not supported. Create a new dataset instead.");
	}

	@Override
	public int indexOf(Object o) {
		return instances.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return instances.lastIndexOf(o);
	}

	@Override
	public ListIterator<TimeSeriesInstance> listIterator() {
		return instances.listIterator();
	}

	@Override
	public ListIterator<TimeSeriesInstance> listIterator(int index) {
		return instances.listIterator(index);
	}

	@Override
	public List<TimeSeriesInstance> subList(int fromIndex, int toIndex) {
		return instances.subList(fromIndex, toIndex);
	}

}