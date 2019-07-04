package ai.libs.jaicore.ml.tsc.filter.transform;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.filter.IFilter;

/**
 * Abstract superclass for all transform filters.
 *
 * @author fischor
 */
public abstract class ATransformFilter implements IFilter {

	@Override
	public TimeSeriesDataset transform(final TimeSeriesDataset input) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double[][] transform(final double[][] input) {
		double[][] transformed = new double[input.length][];
		for (int i = 0; i < input.length; i++) {
			transformed[i] = this.transform(input[i]);
		}
		return transformed;
	}

	@Override
	public void fit(final TimeSeriesDataset input) {
		// Do nothing.
	}

	@Override
	public void fit(final double[] input) {
		// Do nothing.
	}

	@Override
	public void fit(final double[][] input) {
		// Do nothing.
	}

	@Override
	public TimeSeriesDataset fitTransform(final TimeSeriesDataset input) {
		return this.transform(input);
	}

	@Override
	public double[] fitTransform(final double[] input) {
		return this.transform(input);
	}

	@Override
	public double[][] fitTransform(final double[][] input) {
		return this.transform(input);
	}

}