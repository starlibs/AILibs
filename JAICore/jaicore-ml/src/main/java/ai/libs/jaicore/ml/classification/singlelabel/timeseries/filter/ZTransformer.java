/**
 *
 */
package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import java.util.ArrayList;
import java.util.List;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.classification.singlelabel.timeseries.exception.NoneFittedFilterExeception;

/**
 * @author Helen Beierling
 *         This class normalizes the mean of an instance to be zero and the deviation to be one.
 *         s.https://jmotif.github.io/sax-vsm_site/morea/algorithm/znorm.html
 *         one loop: https://www.strchr.com/standard_deviation_in_one_pass?allcomments=1
 *
 * XXX: Duplicates functionality in ai.libs.jaicore.basic.transform.vector.ZTransform
 */
public class ZTransformer extends AFilter {

	private double mean;
	private double deviation;
	private List<double[][]> ztransformedDataset = new ArrayList<>();

	// To get a unbiased estimate for the variance the intermediated results are
	// divided by n-1 instead of n(Number of samples of Population)
	private boolean basselCorrected = true;

	private boolean fitted = false;
	private boolean fittedInstance = false;
	private boolean fittedMatrix = false;

	public void setBasselCorrected(final boolean basselCorrected) {
		this.basselCorrected = basselCorrected;
	}
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */

	@Override
	public TimeSeriesDataset2 transform(final TimeSeriesDataset2 input) {
		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}
		if (!this.fitted) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transform method.");
		}

		for (int matrix = 0; matrix < input.getNumberOfVariables(); matrix++) {

			this.ztransformedDataset.add(this.fitTransform(input.getValues(matrix)));
			this.fittedMatrix = false;
		}
		this.fitted = false;
		return new TimeSeriesDataset2(this.ztransformedDataset);
	}

	@Override
	public void fit(final TimeSeriesDataset2 input) {

		if (input.isEmpty()) {
			throw new IllegalArgumentException("This method can not work with an empty dataset.");
		}

		this.fitted = true;
	}

	@Override
	public TimeSeriesDataset2 fitTransform(final TimeSeriesDataset2 input) {
		this.fit(input);
		return this.transform(input);
	}

	@Override
	public double[] transform(final double[] input) {
		if (!this.fittedInstance) {
			throw new NoneFittedFilterExeception("The fit method must be called before the transfom method is called");
		}
		if (input.length == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty");
		}

		double[] ztransform = new double[input.length];
		for (int entry = 0; entry < input.length; entry++) {
			if (this.deviation != 0) {
				ztransform[entry] = (entry - this.mean) / this.deviation;
			}
		}
		this.fittedInstance = false;
		return ztransform;
	}

	@Override
	public void fit(final double[] input) {
		double sumSq = 0.0;
		double sumMean = 0.0;
		double numberOfEntrys = input.length;

		if (numberOfEntrys == 0) {
			throw new IllegalArgumentException("The to transform instance can not be empty.");
		}
		for (int entry = 0; entry < input.length; entry++) {
			sumSq = sumSq + Math.pow(input[entry], 2);
			sumMean = sumMean + input[entry];
		}
		this.mean = sumMean / numberOfEntrys;
		double variance = (1 / numberOfEntrys) * (sumSq) - Math.pow(this.mean, 2);
		if (this.basselCorrected) {
			double tmp = (numberOfEntrys / (numberOfEntrys - 1));
			this.deviation = Math.sqrt(tmp * variance);
		} else {

			this.deviation = Math.sqrt(variance);
		}

		this.fittedInstance = true;
	}

	@Override
	public double[] fitTransform(final double[] input) {
		this.fit(input);
		return this.transform(input);
	}

	@Override
	public double[][] transform(final double[][] input) {
		if (!this.fittedMatrix) {
			throw new NoneFittedFilterExeception("The fit method must be called first.");
		}
		double[][] ztransformedMatrix = new double[input.length][input[0].length];
		for (int instance = 0; instance < input.length; instance++) {
			ztransformedMatrix[instance] = this.fitTransform(input[instance]);
			this.fittedInstance = false;
		}
		this.fittedMatrix = false;
		return ztransformedMatrix;
	}

	@Override
	public void fit(final double[][] input) {
		this.fittedMatrix = true;
	}

	@Override
	public double[][] fitTransform(final double[][] input) {
		this.fit(input);
		return this.transform(input);
	}

}
