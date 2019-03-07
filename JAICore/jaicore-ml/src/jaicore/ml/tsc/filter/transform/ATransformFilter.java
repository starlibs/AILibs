package jaicore.ml.tsc.filter.transform;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.IFilter;

/**
 * ATransformFilter
 */
public abstract class ATransformFilter implements IFilter {

    // Transform.

    @Override
    public TimeSeriesDataset transform(TimeSeriesDataset input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract double[] transform(double[] input);

    @Override
    public double[][] transform(double[][] input) {
        double[][] transformed = new double[input.length][];
        for (int i = 0; i < input.length; i++) {
            transformed[i] = transform(input[i]);
        }
        return transformed;
    }

    // Fit.

    @Override
    public void fit(TimeSeriesDataset input) {
        // Do nothing.
    }

    @Override
    public void fit(double[] input) {
        // Do nothing.
    }

    @Override
    public void fit(double[][] input) {
        // Do nothing.
    }

    // Fit and transform.

    @Override
    public TimeSeriesDataset fitTransform(TimeSeriesDataset input) {
        return transform(input);
    }

    @Override
    public double[] fitTransform(double[] input) {
        return transform(input);
    }

    @Override
    public double[][] fitTransform(double[][] input) {
        return transform(input);
    }

}