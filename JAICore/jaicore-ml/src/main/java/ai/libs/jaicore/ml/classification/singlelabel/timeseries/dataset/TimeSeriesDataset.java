package ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.api4.java.ai.ml.core.dataset.schema.ILabeledInstanceSchema;
import org.api4.java.ai.ml.core.dataset.schema.attribute.IAttribute;
import org.api4.java.ai.ml.core.dataset.schema.attribute.ITimeseriesAttribute;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.exception.DatasetCreationException;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.attribute.NDArrayTimeseriesAttribute;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.model.INDArrayTimeseries;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.model.NDArrayTimeseries;
import ai.libs.jaicore.ml.core.dataset.ADataset;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset extends ADataset<ITimeSeriesInstance> implements ILabeledDataset<ITimeSeriesInstance> {

	private static final long serialVersionUID = -6819487387561457394L;

	/** Values of time series variables. */
	private List<INDArray> valueMatrices;

	/** Timestamps of time series variables. */
	private List<INDArray> timestampMatrices;

	/** Target values for the instances. */
	private transient List<Object> targets;

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
	public TimeSeriesDataset(final ILabeledInstanceSchema schema, final List<INDArray> valueMatrices, final List<INDArray> timestampMatrices, final List<Object> targets) {
		this(schema);
		for (IAttribute att : schema.getAttributeList()) {
			if (!(att instanceof ITimeseriesAttribute)) {
				throw new IllegalArgumentException("The schema contains attributes which are not timeseries");
			}
		}

		Set<Object> valueInstances = valueMatrices.stream().map(x -> x.shape()[0]).collect(Collectors.toSet());
		if (valueInstances.size() > 1) {
			throw new IllegalArgumentException("The value matrices vary in length i.e. they have different number of instances");
		}

		Set<Object> timestampInstances = timestampMatrices.stream().map(x -> x.shape()[0]).collect(Collectors.toSet());
		if (timestampInstances.size() > 1) {
			throw new IllegalArgumentException("The timestamp matrices vary in length i.e. they have different number of instances");
		}

		valueInstances.addAll(timestampInstances);
		if (valueInstances.size() > 1) {
			throw new IllegalArgumentException("There are different number of instances for values and timestamps");
		}

		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
	}

	public TimeSeriesDataset(final ILabeledInstanceSchema schema) {
		super(schema);
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
		this.addAttribute(attributeName, valueMatrix);
	}

	/**
	 * Removes the time series variable at a given index.
	 *
	 * @param index
	 * @return
	 * @throws IndexOutOfBoundsException
	 */
	@Override
	public void removeColumn(final int index) {
		this.valueMatrices.remove(index);
		this.timestampMatrices.remove(index);
		this.getInstanceSchema().removeAttribute(index);
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
		NDArrayTimeseriesAttribute type = this.createAttribute("ts" + index, valueMatrix);
		this.getInstanceSchema().removeAttribute(index);
		this.getInstanceSchema().addAttribute(index, type);
	}

	public Object getTargets() {
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
		return this.valueMatrices.get(0).shape()[0];
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

	private NDArrayTimeseriesAttribute createAttribute(final String name, final INDArray valueMatrix) {
		int length = (int) valueMatrix.shape()[1];
		return new NDArrayTimeseriesAttribute(name, length);
	}

	private void addAttribute(final String name, final INDArray valueMatrix) {
		NDArrayTimeseriesAttribute type = this.createAttribute(name, valueMatrix);
		this.getInstanceSchema().addAttribute(type);
		this.valueMatrices.add(valueMatrix);
	}

	// --
	// IDataset interface.
	// --

	/**
	 * Iterator for the @{@link}TimeSeriesDataset. Iterates and implicitly creates
	 * the @{link}TimeSeriesInstance.
	 */
	class TimeSeriesDatasetIterator implements Iterator<ITimeSeriesInstance> {
		private int current = 0;

		@Override
		public boolean hasNext() {
			return TimeSeriesDataset.this.getNumberOfInstances() > this.current;
		}

		@Override
		public ITimeSeriesInstance next() {
			if (!this.hasNext()) {
				throw new NoSuchElementException();
			}

			return TimeSeriesDataset.this.get(this.current++);
		}
	}

	@Override
	public TimeSeriesInstance get(final int index) {
		// Build attribute value as view on the row of the attribute matrix.
		List<INDArrayTimeseries> attributeValues = new ArrayList<>();
		for (int i = 0; i < TimeSeriesDataset.this.valueMatrices.size(); i++) {
			attributeValues.add(new NDArrayTimeseries(TimeSeriesDataset.this.valueMatrices.get(i).getRow(index)));
		}
		// Build target value.
		Object target = this.targets.get(index);
		return new TimeSeriesInstance(attributeValues, target);
	}

	@Override
	public Iterator<ITimeSeriesInstance> iterator() {
		return new TimeSeriesDatasetIterator();
	}

	@Override
	public TimeSeriesDataset createEmptyCopy() throws DatasetCreationException, InterruptedException {
		return new TimeSeriesDataset(this.getInstanceSchema());
	}

	@Override
	public Object[][] getFeatureMatrix() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] getLabelVector() {
		return this.targets.toArray();
	}

	@Override
	public TimeSeriesDataset createCopy() throws DatasetCreationException, InterruptedException {
		TimeSeriesDataset copy = this.createEmptyCopy();
		for (ITimeSeriesInstance i : this) {
			copy.add(i);
		}
		return copy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.targets == null) ? 0 : this.targets.hashCode());
		result = prime * result + ((this.timestampMatrices == null) ? 0 : this.timestampMatrices.hashCode());
		result = prime * result + ((this.valueMatrices == null) ? 0 : this.valueMatrices.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		TimeSeriesDataset other = (TimeSeriesDataset) obj;
		if (this.targets == null) {
			if (other.targets != null) {
				return false;
			}
		} else if (!this.targets.equals(other.targets)) {
			return false;
		}
		if (this.timestampMatrices == null) {
			if (other.timestampMatrices != null) {
				return false;
			}
		} else if (!this.timestampMatrices.equals(other.timestampMatrices)) {
			return false;
		}
		if (this.valueMatrices == null) {
			if (other.valueMatrices != null) {
				return false;
			}
		} else if (!this.valueMatrices.equals(other.valueMatrices)) {
			return false;
		}
		return true;
	}
}
