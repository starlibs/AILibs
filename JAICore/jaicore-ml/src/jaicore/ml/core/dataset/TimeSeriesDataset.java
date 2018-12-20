package jaicore.ml.core.dataset;

import java.util.List;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Time Series Dataset.
 */
public class TimeSeriesDataset {

	/** Number of instances contained in the dataset. */
	private long numberOfInstances;

	/** Values of time series variables. */
	private List<INDArray> valueMatrices;

	/** Timestamps of time series variables. */
	private List<INDArray> timestampMatrices;

	/** Target values for the instances. */
	private INDArray targets;

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
		this.numberOfInstances = valueMatrices.size();
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
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

}
