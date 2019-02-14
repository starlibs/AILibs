package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * DerivateTransformDistance
 */
public class DerivateTransformDistance implements ITimeSeriesDistance {

    private double alpha;
    private double a;
    private double b;

    private ITimeSeriesDistance timeSeriesDistance;
    private ITimeSeriesDistance derivateDistance;

    public DerivateTransformDistance(double alpha, ITimeSeriesDistance timeSeriesDistance,
            ITimeSeriesDistance derivateDistance) {
        // Parameter checks.
        if (alpha > Math.PI / 2 || alpha < 0) {
            throw new IllegalArgumentException("Parameter alpha has to be between 0 and 1.");
        }
        setAlpha(alpha);
        this.timeSeriesDistance = timeSeriesDistance;
        this.derivateDistance = derivateDistance;
    }

    public DerivateTransformDistance(double alpha, ITimeSeriesDistance distance) {
        this(alpha, distance, distance);
    }

    public DerivateTransformDistance(double alpha) {
        this(alpha, new DynamicTimeWarping());
    }

    @Override
    public double distance(double[] A, double[] B) {
        double[] derivateA = TimeSeriesUtil.backwardDifferenceDerivate(A);
        double[] derivateB = TimeSeriesUtil.backwardDifferenceDerivate(B);

        return a * timeSeriesDistance.distance(A, B) + b * derivateDistance.distance(derivateA, derivateB);
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        this.a = Math.cos(alpha);
        this.b = Math.sin(alpha);
    }

    public double getAlpha() {
        return alpha;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

}