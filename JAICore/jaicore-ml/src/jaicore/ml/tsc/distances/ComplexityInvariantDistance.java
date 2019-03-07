package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.complexity.ITimeSeriesComplexity;

/**
 * Implementation of the Complexity Invariant Distance (CID) measure as
 * published in "A Complexity-Invariant Distance Measure for Time Series" by
 * Gustavo E.A.P.A. Batista, Xiaoyue Wang and Eamonn J. Keogh.
 * 
 * The authors address the <i>complexity</i> invariant of time series distance
 * measures. That is, that time series with higher complexity tend to be further
 * apart under current distance measures than pairs of simple objects.
 * 
 * Given a complexity measure <code>c</code> and a distance measure
 * <code>d</code>, the Complexity Invariant Distance for the two time series
 * <code>A</code> and <code>B</code> is:
 * <code>d(A, B) * (max(c(A), c(B)) / min(c(A), c(B)))</code>.
 */
public class ComplexityInvariantDistance implements ITimeSeriesDistance {

    /** The distance measure to make complexity invariant. */
    private ITimeSeriesDistance distanceMeasure;

    /** The complexity measure. */
    private ITimeSeriesComplexity complexityMeasure;

    /**
     * Constructor.
     * 
     * @param distance   The distance measure to make complexity invariant.
     * @param complexity The complexity measure.
     */
    ComplexityInvariantDistance(ITimeSeriesDistance distanceMeasure, ITimeSeriesComplexity complexityMeasure) {
        // Parameter checks.
        if (distanceMeasure == null)
            throw new IllegalArgumentException("The distance measure must not be null.");
        if (complexityMeasure == null)
            throw new IllegalArgumentException("The complexity measure must not be null.");

        this.distanceMeasure = distanceMeasure;
        this.complexityMeasure = complexityMeasure;
    }

    @Override
    public double distance(double[] A, double[] B) {
        double complexityA = complexityMeasure.complexity(A);
        double complexityB = complexityMeasure.complexity(B);
        double complexityCorrectionFactor = Math.max(complexityA, complexityB) / Math.min(complexityA, complexityB);

        return distanceMeasure.distance(A, B) * complexityCorrectionFactor;
    }

}