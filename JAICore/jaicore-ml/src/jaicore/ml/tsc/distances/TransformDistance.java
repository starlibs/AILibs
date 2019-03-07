package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.filter.transform.ATransformFilter;

/**
 * TransformDistance
 */
public class TransformDistance implements ITimeSeriesDistance {

    private double alpha;
    private double a;
    private double b;

    private ATransformFilter transform;
    private ITimeSeriesDistance timeSeriesDistance;
    private ITimeSeriesDistance transformDistance;

    public TransformDistance(double alpha, ATransformFilter transform, ITimeSeriesDistance timeSeriesDistance,
            ITimeSeriesDistance transformDistance) {
        // Parameter checks.
        if (alpha > Math.PI / 2 || alpha < 0)
            throw new IllegalArgumentException("Parameter alpha has to be between 0 (inclusive) and pi/2 (inclusive).");
        if (transform == null)
            throw new IllegalArgumentException("Parameter transform must not be null.");
        if (timeSeriesDistance == null)
            throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
        if (transformDistance == null)
            throw new IllegalArgumentException("Parameter transformDistance must not be null.");

        this.alpha = alpha;
        this.a = Math.cos(alpha);
        this.b = Math.sin(alpha);
    }

    @Override
    public double distance(double[] A, double[] B) {
        double[] transformA = this.transform.transform(A);
        double[] transformB = this.transform.transform(B);

        return a * timeSeriesDistance.distance(A, B) + b * transformDistance.distance(transformA, transformB);
    }

}