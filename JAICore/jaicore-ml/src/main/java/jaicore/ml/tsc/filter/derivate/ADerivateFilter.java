package jaicore.ml.tsc.filter.derivate;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.filter.IFilter;

/**
 * Abstract superclass for all derivate filters.
 * 
 * @author fischor
 */
public abstract class ADerivateFilter implements IFilter {

    /**
     * Flag that states wheter the filter should add a padding to the derivate
     * assure that is has the same length as the origin time series or not.
     */
    protected boolean withBoundaries;

    public ADerivateFilter(boolean withBoundaries) {
        this.withBoundaries = withBoundaries;
    }

    public ADerivateFilter() {
        this.withBoundaries = false;
    }

    /**
     * Calculates the derivate of a time series.
     * 
     * @param T The time series to calculate the derivate for.
     * @return The derivate of the time series.
     */
    protected abstract double[] derivate(double[] T);

    /**
     * Calcuates the derivates of a time series. In contrast to the normal
     * {@link derivate} calculation, this method is guaranteed to return a derivate
     * that has the same length than the original time series. This is accomplished
     * via padding.
     * 
     * @param T The time series to calculate the derivate for.
     * @return The, possibly padded, derivate of the time series.
     */
    protected abstract double[] derivateWithBoundaries(double[] T);

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