package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.complexity.ITimeSeriesComplexity;

/**
 * ComplexityInvariantDistance
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