package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import java.util.ArrayList;
import java.util.Arrays;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.exception.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *         This class cuts an instance or a set of instances into a number of smaller instances which are
 *         typically saved in an matrix per instance and the matrices in a list.
 *         c.f. p.1508 "The BOSS is concerned with time series classification in the presence of noise" by Patrick Schaefer
 */
public class SlidingWindowBuilder implements IFilter {

	private static final String MSG_SPECIALFIT = "This is done by the special fit and transform because this mehtod must return a new dataset not a double array.";

	private boolean fitted = false;
	private boolean fittedMatrix = false;

	private int defaultWindowSize = 20;

	private ArrayList<double[][]> blownUpDataset = new ArrayList<>();
	private double[][] blownUpMatrix = null;

	public void setDefaultWindowSize(final int defaultWindowSize) {
		this.defaultWindowSize = defaultWindowSize;
	}

	public int getDefaultWindowSize() {
		return this.defaultWindowSize;
	}

	@Override
	public TimeSeriesDataset2 transform(final TimeSeriesDataset2 input) {
		if (input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty");
		}

		if (!this.fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before transformning");
		}

		return new TimeSeriesDataset2(this.blownUpDataset, null, null);
	}

	@Override
	// Results in a list of matrices where each instance has its own matrix.
	// Therefore the structure of the matrices are lost if this method is used.
	public void fit(final TimeSeriesDataset2 input) {
		for (int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {
			ArrayList<double[][]> newMatrices = new ArrayList<>();
			for (double[] instance : input.getValues(matrix)) {
				double[][] newMatrix = new double[(instance.length - this.defaultWindowSize)][this.defaultWindowSize];
				for (int entry = 0; entry < instance.length - this.defaultWindowSize; entry++) {
					double[] tmp = Arrays.copyOfRange(instance, entry, entry + this.defaultWindowSize);
					newMatrix[entry] = tmp;
				}
				newMatrices.add(newMatrix);
			}

			this.blownUpDataset = newMatrices;
		}
		this.fitted = true;
	}

	/**
	 * This is an extra fit method because it does not return a double[] array even though it gets
	 * a double [] as input as it would be defined in the .
	 *
	 * @param instance that has to be transformed
	 * @return the tsdataset that results from one instance which consists of
	 *         one matrix with each row represents one part of the instance from i to i+ window length for i < n- window length
	 */
	public TimeSeriesDataset2 specialFitTransform(final double[] instance) {
		if (instance.length == 0) {
			throw new IllegalArgumentException("The input instance can not be empty");
		}
		if (instance.length < this.defaultWindowSize) {
			throw new IllegalArgumentException("The input instance can not be smaller than the windowsize");
		}

		double[][] newMatrix = new double[instance.length - this.defaultWindowSize + 1][this.defaultWindowSize];

		for (int entry = 0; entry <= instance.length - (this.defaultWindowSize); entry++) {
			newMatrix[entry] = Arrays.copyOfRange(instance, entry, entry + this.defaultWindowSize);
		}
		ArrayList<double[][]> newDataset = new ArrayList<>();
		newDataset.add(newMatrix);
		return new TimeSeriesDataset2(newDataset);
	}

	@Override
	public TimeSeriesDataset2 fitTransform(final TimeSeriesDataset2 input) {
		this.fit(input);
		return this.transform(input);
	}

	/*
	 * This operation is unsupported because it would result in one stream of new instances in one array.
	 */
	@Override
	public double[] transform(final double[] input) {
		throw new UnsupportedOperationException(MSG_SPECIALFIT);
	}

	/*
	 * This method is unsupported because the corresponding transform operation is
	 * not useful
	 */
	@Override
	public void fit(final double[] input) {
		throw new UnsupportedOperationException(MSG_SPECIALFIT);

	}

	@Override
	public double[] fitTransform(final double[] input) {
		throw new UnsupportedOperationException(MSG_SPECIALFIT);

	}

	@Override
	public double[][] transform(final double[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException("The input matrix can not be empty");
		}

		if (!this.fittedMatrix) {
			throw new NoneFittedFilterExeception("The fit mehtod must be called before transformning");
		}

		return this.blownUpMatrix;
	}

	@Override
	// Does not return a list of matrices but a bigger matrix where the new created instances are getting stacked
	// if there is a instance of size n than the first n-window length rows are the sliced instance.
	public void fit(final double[][] input) {
		if (input.length == 0) {
			throw new IllegalArgumentException("The input matrix can not be empty");
		}

		// This is the buffer for the new matrix that gets created from a single instance.
		this.blownUpMatrix = new double[input.length * (input[0].length - this.defaultWindowSize)][this.defaultWindowSize];
		for (int instance = 0; instance < input.length; instance++) {
			for (int entry = 0; entry < input[instance].length - this.defaultWindowSize; entry++) {
				// Every entry in the new matrix is equal to a copy of the original instance from
				// entry i to entry i plus window length.
				this.blownUpMatrix[instance + (entry)] = Arrays.copyOfRange(input[instance], entry, entry + this.defaultWindowSize);
			}
		}
		this.fittedMatrix = true;
	}

	@Override
	public double[][] fitTransform(final double[][] input) {
		this.fit(input);
		return this.transform(input);
	}

}
