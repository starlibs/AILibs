package jaicore.ml.tsc.dataset;

import java.util.List;

import jaicore.ml.tsc.util.ClassMapper;

/**
 * Dataset for time series.
 *
 * <p>
 * The dataset consists of a value matrices and timestamp matrices. In the
 * univariate case, there exists one value matrix, either with or without a
 * corresponding timestamp matrix. In the multivariate case there exists
 * multiple value matrices, each either with or without a corresponding
 * timestamp matrix. Each value matrix is associated with an integer index. Each
 * timestamp matrix is associated with an integer index. Two corresponding value
 * and timestamp matrices are associated with the same index. The dimensions of
 * two corresponding value and timestamp matrices are assured to be the same.
 * All value matrices have the same number of rows, but not necessarily the same
 * number of columns. The <code>i</code>-th row of each matrix corresponds to
 * the <code>i</code>-th instance of the dataset.
 * <p>
 * <p>
 * The targets contained in this dataset are always integers. The can be mapped
 * back and forth with the {@link ClassMapper}. Targets are represented as an
 * integer array. The <code>i</code>-th entry of this array corresponds to the
 * <code>i</code>-th instance of the dataset.
 * </p>
 *
 * @author fischor
 */
public class TimeSeriesDataset {

	/** Number of instances contained in the dataset. */
	private int numberOfInstances;

	/** Values of time series variables. */
	private List<double[][]> valueMatrices;

	/** Timestamps of time series variables. */
	private List<double[][]> timestampMatrices;

	/** Target values for the instances. */
	private int[] targets;

	/** States, whether in train (or test) mode. */
	private final boolean train;

	/**
	 * Creates a time series dataset with timestamps for training. Let `n` be the
	 * number of instances.
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
	public TimeSeriesDataset(final List<double[][]> valueMatrices, final List<double[][]> timestampMatrices, final int[] targets) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).length;
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = targets;
		this.train = true;
	}

	/**
	 * Creates a time series dataset with timestamps for testing. Let `n` be the
	 * number of instances.
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
	public TimeSeriesDataset(final List<double[][]> valueMatrices, final List<double[][]> timestampMatrices) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).length;
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = timestampMatrices;
		this.targets = new int[this.numberOfInstances];
		this.train = false;
	}

	/**
	 * Creates a time series dataset withot timestamps for training. Let `n` be the
	 * number of instances.
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
	public TimeSeriesDataset(final List<double[][]> valueMatrices, final int[] targets) {
		// Parameter checks.
		// ..
		this(valueMatrices, null, targets);
	}

	/**
	 * Creates a time series dataset without timestamps for testing. Let `n` be the
	 * number of instances.
	 *
	 * @param valueMatrices Values for the time series variables. List of 2D-Arrays
	 *                      with shape `[n, ?]`.
	 * @param targets       Target values for the instances.
	 */
	public TimeSeriesDataset(final List<double[][]> valueMatrices) {
		// Parameter checks.
		// ..
		this.numberOfInstances = valueMatrices.get(0).length;
		this.valueMatrices = valueMatrices;
		this.timestampMatrices = null;
		this.targets = new int[this.numberOfInstances];
		this.train = false;

	}

	/**
	 * Add a time series variable with timestamps to the dataset.
	 *
	 * @param valueMatrix     Values for the time series variable to add. 2D-Arrays
	 *                        with shape `[n, ?]` where `n` is the number of
	 *                        instances of the dataset.
	 * @param timestampMatrix Timestamps for the time series variable to add.
	 *                        2D-Arrays with shape `[n, ?]` where `n` is the number
	 *                        of instances of the dataset. Or `null` if no timestamp
	 *                        exists for this time series variable.
	 */
	public void add(final double[][] valueMatrix, final double[][] timestampMatrix) {
		// Parameter checks.
		// ..
		this.valueMatrices.add(valueMatrix);
		this.timestampMatrices.add(timestampMatrix);
	}

	/**
	 * Add a time series variable without timestamps to the dataset.
	 *
	 * @param valueMatrix Values for the time series variable to add. 2D-Arrays with
	 *                    shape `[n, ?]` where `n` is the number of instances of the
	 *                    dataset.
	 */
	public void add(final double[][] valueMatrix) {
		// Parameter checks.
		// ..
		this.valueMatrices.add(valueMatrix);
		this.timestampMatrices.add(null);
	}

	/**
	 * Removes the time series variable at a given index.
	 *
	 * @param index
	 * @throws IndexOutOfBoundsException
	 */
	public void remove(final int index) {
		this.valueMatrices.remove(index);
		this.timestampMatrices.remove(index);
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
	public void replace(final int index, final double[][] valueMatrix, final double[][] timestampMatrix) {
		this.valueMatrices.set(index, valueMatrix);
		if (timestampMatrix != null && this.timestampMatrices != null && this.timestampMatrices.size() > index) {
			this.timestampMatrices.set(index, timestampMatrix);
		}
	}

	/**
	 * Getter for the target values.
	 *
	 * @return The targets.
	 */
	public int[] getTargets() {
		return this.targets;
	}

	/**
	 * Returns the number of variables, i.e. the number of value matrices contained
	 * in the dataset.
	 *
	 * @return The number of variables.
	 */
	public int getNumberOfVariables() {
		return this.valueMatrices.size();
	}

	/**
	 * Returns the number of instances contained in the dataset.
	 *
	 * @return The number of instances contained in the dataset.
	 */
	public int getNumberOfInstances() {
		return this.numberOfInstances;
	}

	/**
	 * Getter for the value matrix at a specific index. Throws an exception if no
	 * timestamp matrix exists at this index.
	 *
	 * @param index The index of the value matrix.
	 * @return The value matrix at index <code>index</code>.
	 * @throws IndexOutOfBoundsException If there is no value matrix at index
	 *                                   <code>index</code>.
	 */
	public double[][] getValues(final int index) {
		return this.valueMatrices.get(index);
	}

	/**
	 * Getter for the timestamp matrix at a specific index.
	 *
	 * @param index The index of the timestamp matrix.
	 * @return The timestamp matrix at index <code>index</code>.
	 * @throws IndexOutOfBoundsException If there is no value timestamp at index
	 *                                   <code>index</code>.
	 */
	public double[][] getTimestamps(final int index) {
		return this.timestampMatrices.get(index);
	}

	/**
	 * Getter for the value matrix at a specific index.
	 *
	 * @param index The index of the timestamp matrix.
	 * @return The value matrix at index <code>index</code>. Or <code>null</code>,
	 *         if no value matrix exists at index <code>index</code>.
	 */
	public double[][] getValuesOrNull(final int index) {
		return this.valueMatrices.size() > index ? this.valueMatrices.get(index) : null;
	}

	/**
	 * Getter for the timestamp matrix at a specific index.
	 *
	 * @param index The index of the timestamp matrix.
	 * @return The timestamp matrix at index <code>index</code>. Or
	 *         <code>null</code>, if no timestamp matrix exists at index
	 *         <code>index</code>.
	 */
	public double[][] getTimestampsOrNull(final int index) {
		return this.timestampMatrices.size() > index ? this.timestampMatrices.get(index) : null;
	}

	/**
	 * States whether the dataset is empty, i.e. contains no value matrices, or not.
	 *
	 * @return <code>True</code>, if the dataset is empty. <code>False</code>,
	 *         otherwise.
	 */
	public boolean isEmpty() {
		return this.valueMatrices.isEmpty();
	}

	/**
	 * States whether the dataset is a univariate dataset, i.e. contains exactly one
	 * value matrix, or not.
	 *
	 * @return <code>True</code>, if the dataset is univariate. <code>False</code>,
	 *         otherwise.
	 */
	public boolean isUnivariate() {
		return this.valueMatrices.size() == 1;
	}

	/**
	 * States whether the dataset is a univariate dataset, i.e. contains more than
	 * one value matrix, or not.
	 *
	 * @return <code>True</code>, if the dataset is multivariate.
	 *         <code>False</code>, otherwise.
	 */
	public boolean isMultivariate() {
		return this.valueMatrices.size() > 1;
	}

	/**
	 * States whether the dataset is a training dataset, i.e. contains valid targets
	 * after initialization, or not.
	 *
	 * @return <code>True</code>, if the dataset is a training dataset.
	 *         <code>False</code>, otherwise.
	 */
	public boolean isTrain() {
		return this.train;
	}

	/**
	 * States whether the dataset is a test dataset, i.e. contains no valid targets
	 * after initialization, or not.
	 *
	 * @return <code>True</code>, if the dataset is a test dataset.
	 *         <code>False</code>, otherwise.
	 */
	public boolean isTest() {
		return !this.train;
	}

	/**
	 * Getter for {@link TimeSeriesDataset#valueMatrices}.
	 *
	 * @return the valueMatrices
	 */
	public List<double[][]> getValueMatrices() {
		return this.valueMatrices;
	}

	/**
	 * Getter for {@link TimeSeriesDataset#timestampMatrices}.
	 *
	 * @return the timestampMatrices
	 */
	public List<double[][]> getTimestampMatrices() {
		return this.timestampMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#valueMatrices}.
	 *
	 * @param valueMatrices the valueMatrices to set
	 */
	public void setValueMatrices(final List<double[][]> valueMatrices) {
		this.valueMatrices = valueMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#timestampMatrices}.
	 *
	 * @param timestampMatrices the timestampMatrices to set
	 */
	public void setTimestampMatrices(final List<double[][]> timestampMatrices) {
		this.timestampMatrices = timestampMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#targets}.
	 *
	 * @param targets the targets to set
	 */
	public void setTargets(final int[] targets) {
		this.targets = targets;
	}
}