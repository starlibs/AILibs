package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.filter.transform.ATransformFilter;
import jaicore.ml.tsc.filter.transform.CosineTransform;

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

        setAlpha(alpha);
        this.transform = transform;
        this.timeSeriesDistance = timeSeriesDistance;
        this.transformDistance = transformDistance;
    }

    public TransformDistance(double alpha, ATransformFilter transform, ITimeSeriesDistance distance) {
        this(alpha, transform, distance, distance);
    }

    public TransformDistance(double alpha, ITimeSeriesDistance timeSeriesDistance,
            ITimeSeriesDistance transformDistance) {
        this(alpha, new CosineTransform(), timeSeriesDistance, transformDistance);
    }

    public TransformDistance(double alpha, ITimeSeriesDistance distance) {
        this(alpha, new CosineTransform(), distance);
    }

    @Override
    public double distance(double[] A, double[] B) {
        double[] transformA = this.transform.transform(A);
        double[] transformB = this.transform.transform(B);

        return a * timeSeriesDistance.distance(A, B) + b * transformDistance.distance(transformA, transformB);
    }

    /**
     * Sets the alpha value and adjusts the measurement parameters
     * <code>a = cos(alpha)<code> and <code>b = sin(alpha)</code> accordingly.
     * 
     * @param alpha The alpha value, <code>0 <= alpha <= pi/2</code>.
     */
    public void setAlpha(double alpha) {
        // Parameter checks.
        if (alpha > Math.PI / 2 || alpha < 0) {
            throw new IllegalArgumentException("Parameter alpha has to be between 0 (inclusive) and pi/2 (inclusive).");
        }

        this.alpha = alpha;
        this.a = Math.cos(alpha);
        this.b = Math.sin(alpha);
    }

    /**
     * Getter for the alpha value. It holds, that <code>0 <= alpha <= pi/2</code>.
     */
    public double getAlpha() {
        return alpha;
    }

    /**
     * Getter for the <code>a</code> parameter. @see #a
     */
    public double getA() {
        return a;
    }

    /**
     * Getter for the <code>a</code> parameter. @see #b
     */
    public double getB() {
        return b;
    }

}