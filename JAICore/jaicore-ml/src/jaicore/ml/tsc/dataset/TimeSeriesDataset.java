package jaicore.ml.tsc.dataset;

import java.util.List;

/**
 * UnivariateDataset
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
    public TimeSeriesDataset(List<double[][]> valueMatrices, List<double[][]> timestampMatrices, int[] targets) {
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
    public TimeSeriesDataset(List<double[][]> valueMatrices, List<double[][]> timestampMatrices) {
        // Parameter checks.
        // ..
        this.numberOfInstances = valueMatrices.get(0).length;
        this.valueMatrices = valueMatrices;
        this.timestampMatrices = timestampMatrices;
        this.targets = new int[numberOfInstances];
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
    public TimeSeriesDataset(List<double[][]> valueMatrices, int[] targets) {
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
    public TimeSeriesDataset(List<double[][]> valueMatrices) {
        // Parameter checks.
        // ..
        this.numberOfInstances = valueMatrices.get(0).length;
        this.valueMatrices = valueMatrices;
        this.timestampMatrices = null;
        this.targets = new int[numberOfInstances];
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
    public void add(double[][] valueMatrix, double[][] timestampMatrix) {
        // Parameter checks.
        // ..
        valueMatrices.add(valueMatrix);
        timestampMatrices.add(timestampMatrix);
    }

    /**
     * Add a time series variable without timestamps to the dataset.
     * 
     * @param valueMatrix Values for the time series variable to add. 2D-Arrays with
     *                    shape `[n, ?]` where `n` is the number of instances of the
     *                    dataset.
     */
    public void add(double[][] valueMatrix) {
        // Parameter checks.
        // ..
        valueMatrices.add(valueMatrix);
        timestampMatrices.add(null);
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
    public void replace(int index, double[][] valueMatrix, double[][] timestampMatrix)
            throws IndexOutOfBoundsException {
        valueMatrices.set(index, valueMatrix);
        if (timestampMatrix != null && timestampMatrices != null && timestampMatrices.size() > index)
            timestampMatrices.set(index, timestampMatrix);
    }

    public int[] getTargets() {
        return targets;
    }

    public int getNumberOfVariables() {
        return valueMatrices.size();
    }

    public int getNumberOfInstances() {
        return numberOfInstances;
    }

    public double[][] getValues(int index) throws IndexOutOfBoundsException {
        return valueMatrices.get(index);
    }

    public double[][] getTimestamps(int index) throws IndexOutOfBoundsException {
        return timestampMatrices.get(index);
    }

    public double[][] getValuesOrNull(int index) {
        return valueMatrices.size() > index ? valueMatrices.get(index) : null;
    }

    public double[][] getTimestampsOrNull(int index) {
        return timestampMatrices != null && timestampMatrices.size() > index ? timestampMatrices.get(index) : null;
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

    public boolean isTrain() {
        return train;
    }

    public boolean isTest() {
        return !train;
    }

	/**
	 * Getter for {@link TimeSeriesDataset#valueMatrices}.
	 * 
	 * @return the valueMatrices
	 */
	public List<double[][]> getValueMatrices() {
		return valueMatrices;
	}

	/**
	 * Getter for {@link TimeSeriesDataset#timestampMatrices}.
	 * 
	 * @return the timestampMatrices
	 */
	public List<double[][]> getTimestampMatrices() {
		return timestampMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#valueMatrices}.
	 * 
	 * @param valueMatrices
	 *            the valueMatrices to set
	 */
	public void setValueMatrices(List<double[][]> valueMatrices) {
		this.valueMatrices = valueMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#timestampMatrices}.
	 * 
	 * @param timestampMatrices
	 *            the timestampMatrices to set
	 */
	public void setTimestampMatrices(List<double[][]> timestampMatrices) {
		this.timestampMatrices = timestampMatrices;
	}

	/**
	 * Setter for {@link TimeSeriesDataset#targets}.
	 * 
	 * @param targets
	 *            the targets to set
	 */
	public void setTargets(int[] targets) {
		this.targets = targets;
	}
}