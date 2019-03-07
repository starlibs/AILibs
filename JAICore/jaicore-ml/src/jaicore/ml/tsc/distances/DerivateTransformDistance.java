package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.filter.derivate.ADerivateFilter;
import jaicore.ml.tsc.filter.transform.ATransformFilter;

/**
 * DerivateTransformDistance
 */
public class DerivateTransformDistance implements ITimeSeriesDistance {

    private double a;
    private double b;
    private double c;
    ADerivateFilter derivate;
    ATransformFilter transform;
    ITimeSeriesDistance dist;

    public DerivateTransformDistance(double a, double b, double c, ADerivateFilter derivate, ATransformFilter transform,
            ITimeSeriesDistance dist) {
        // Parameter checks.
        if (a < 0 || a > 1)
            throw new IllegalArgumentException("Parameter a must be in interval [0,1].");
        if (b < 0 || b > 1)
            throw new IllegalArgumentException("Parameter b must be in interval [0,1].");
        if (c < 0 || c > 1)
            throw new IllegalArgumentException("Parameter c must be in interval [0,1].");
        if (derivate == null)
            throw new IllegalArgumentException("Parameter derivate must not be null.");
        if (transform == null)
            throw new IllegalArgumentException("Parameter transform must not be null.");
        if (dist == null)
            throw new IllegalArgumentException("Parameter dist must not be null.");

        this.a = a;
        this.b = b;
        this.c = c;
        this.derivate = derivate;
        this.transform = transform;
        this.dist = dist;
    }

    @Override
    public double distance(double[] A, double[] B) {
        double[] derivateA = this.derivate.transform(A);
        double[] derivateB = this.derivate.transform(B);
        double[] transformA = this.transform.transform(A);
        double[] transformB = this.transform.transform(B);

        return a * dist.distance(A, B) + b * dist.distance(derivateA, derivateB)
                + c * dist.distance(transformA, transformB);
    }

}