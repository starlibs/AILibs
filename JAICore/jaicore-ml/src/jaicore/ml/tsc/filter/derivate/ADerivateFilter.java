package jaicore.ml.tsc.filter.derivate;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.IFilter;

public abstract class ADerivateFilter implements IFilter {

    protected boolean withBoundaries;

    public ADerivateFilter(boolean withBoundaries) {
        this.withBoundaries = withBoundaries;
    }

    public ADerivateFilter() {
        this.withBoundaries = false;
    }

    protected abstract double[] derivate(double[] T);

    protected abstract double[] derivateWithBoundaries(double[] T);

    // Transform.

    @Override
    public TimeSeriesDataset transform(TimeSeriesDataset input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] transform(double[] input) {
        if (withBoundaries) {
            return derivateWithBoundaries(input);
        } else {
            return derivate(input);
        }
    }

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
    public double[] fitTransformInstance(double[] input) {
        return transform(input);
    }

    @Override
    public double[][] fitTransform(double[][] input) {
        return transform(input);
    }

}