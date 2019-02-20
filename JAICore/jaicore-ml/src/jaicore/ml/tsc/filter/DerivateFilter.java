package jaicore.ml.tsc.filter;

import jaicore.ml.tsc.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

/**
 * DerivateFilter
 */
public class DerivateFilter implements IFilter {

    private boolean boundaries;
    private DerivateType derivateType;

    public enum DerivateType {
        FORWARD_DIFFRENCE, BACKWARD_DIFFERENCE, KEOGH, GULLO
    }

    public DerivateFilter(DerivateType derivateType, boolean withBoundaries) {
        this.derivateType = derivateType;
        this.boundaries = withBoundaries;
    }

    @Override
    public TimeSeriesDataset transform(TimeSeriesDataset input)
            throws IllegalArgumentException, NoneFittedFilterExeception {
        return null;
    }

    @Override
    public void fit(TimeSeriesDataset input) {

    }

    @Override
    public TimeSeriesDataset fitTransform(TimeSeriesDataset input)
            throws IllegalArgumentException, NoneFittedFilterExeception {
        return null;
    }

}