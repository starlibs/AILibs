package jaicore.ml.tsc.distances;

import org.nd4j.linalg.api.ndarray.INDArray;

import static jaicore.ml.tsc.util.TimeSeriesUtil.*;

import jaicore.ml.tsc.complexity.ITimeSeriesComplexity;
import jaicore.ml.tsc.exceptions.TimeSeriesLengthException;

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
    public double distance(INDArray A, INDArray B) throws TimeSeriesLengthException {
        // Parameter checks.
        isTimeSeriesOrException(A, B);
        isSameLengthOrException(A, B);

        double complexityA = complexityMeasure.complexity(A);
        double complexityB = complexityMeasure.complexity(B);
        double complexityCorrectionFactor = Math.max(complexityA, complexityB) / Math.min(complexityA, complexityB);

        return distanceMeasure.distance(A, B) * complexityCorrectionFactor;
    }

}