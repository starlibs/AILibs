package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.filter.derivate.ADerivateFilter;
import jaicore.ml.tsc.filter.derivate.BackwardDifferenceDerivate;
import jaicore.ml.tsc.util.TimeSeriesUtil;

/**
 * Implementation of the Derivate Distance (DD) measure as published in "Using
 * derivatives in time series classification" by Tomasz Gorecki and Maciej
 * Luczak (2013).
 * 
 * The authors wanted to create a distance measure which considers both, the
 * function values of times series (point to point comparison) and the values of
 * their derivates (general shape comparison).
 * 
 * Given a distance measure <code>d</code>, the Derivate Distance for the two
 * time series <code>A</code> and <code>B</code> is:
 * <code>a * d(A, B) + b * d(A', B')</code>, where <code>A'</code> and
 * <code>B'</code> are the derivates (@see jaicore.ml.tsc.filter.derivate) of
 * <code>A</code> and <code>B</code> respec. and
 * <code>0 <= a <= 1, 0 <= b <= 1></code> are parameters of the measure. The
 * parameters <code>a</code> and <code>b</code> are set via an
 * <code>alpha</code> value, that is <code>a=cos(alpha)</code> and
 * <code>b=sin(alpha)</code>.
 * 
 * The Derivate Distance that uses Dynamic Time Warping as underlying distance
 * measure is commonly denoted as DD_DTW. The Derivate Distance that uses the
 * Euclidean distance as underlying distance measure is commonly denoted as
 * DD_ED.
 * <p>
 * It is also possible to use a distinct distance measure to calculate the
 * distance between the time series and its derivates.
 * </p>
 */
public class DerivateDistance implements ITimeSeriesDistance {

    /**
     * Alpha value that adjusts the parameters {@link a} and {@link b}, that is
     * <code>a=cos(alpha)</code> and <code>b=sin(alpha)</code>. It holds, that
     * <code>0 <= alpha <= pi/2</code>.
     */
    private double alpha;

    /**
     * Determines the influence of distance of the function values to the overall
     * distance measure.
     */
    private double a;

    /**
     * Determines the influence of distance of the derivates values to the overall
     * distance measure.
     */
    private double b;

    /** The derivate calculation to use. */
    private ADerivateFilter derivate;

    /**
     * The distance measure to use to calculate the distance of the function values.
     */
    private ITimeSeriesDistance timeSeriesDistance;

    /**
     * The distance measure to use to calculate the distance of the derivate values.
     */
    private ITimeSeriesDistance derivateDistance;

    /**
     * Constructor with individual distance measures for the function and derivate
     * values.
     * 
     * @param alpha              The distance measure to use to calculate the
     *                           distance of the function values.
     *                           <code>0 <= alpha <= pi/2</code>.
     * @param derivate           The derivate calculation to use.
     * @param timeSeriesDistance The distance measure to use to calculate the
     *                           distance of the derivate values.
     * @param derivateDistance   The distance measure to use to calculate the
     *                           distance of the derivate values.
     */
    public DerivateDistance(double alpha, ADerivateFilter derivate, ITimeSeriesDistance timeSeriesDistance,
            ITimeSeriesDistance derivateDistance) {
        // Parameter checks.
        if (alpha > Math.PI / 2 || alpha < 0)
            throw new IllegalArgumentException("Parameter alpha has to be between 0 (inclusive) and pi/2 (inclusive).");
        if (derivate == null)
            throw new IllegalArgumentException("Parameter derivate must not be null.");
        if (timeSeriesDistance == null)
            throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
        if (derivateDistance == null)
            throw new IllegalArgumentException("Parameter derivateDistance must not be null.");

        setAlpha(alpha);
        this.derivate = derivate;
        this.timeSeriesDistance = timeSeriesDistance;
        this.derivateDistance = derivateDistance;
    }

    /**
     * Constructor with individual distance measures for the function and derivate
     * values that uses the {@link BackwardDifferenceDerivate} as derivation.
     * 
     * @param alpha              The distance measure to use to calculate the
     *                           distance of the function values.
     *                           <code>0 <= alpha <= pi/2</code>.
     * @param timeSeriesDistance The distance measure to use to calculate the
     *                           distance of the derivate values.
     * @param derivateDistance   The distance measure to use to calculate the
     *                           distance of the derivate values.
     */
    public DerivateDistance(double alpha, ITimeSeriesDistance timeSeriesDistance,
            ITimeSeriesDistance derivateDistance) {
        this(alpha, new BackwardDifferenceDerivate(), timeSeriesDistance, derivateDistance);
    }

    /**
     * Constructor that uses the same distance measures for the function and
     * derivate values.
     * 
     * @param alpha    The distance measure to use to calculate the distance of the
     *                 function values. <code>0 <= alpha <= pi/2</code>.
     * @param derivate The derivate calculation to use.
     * @param distance The distance measure to use to calculate the distance of the
     *                 function and derivate values.
     */
    public DerivateDistance(double alpha, ADerivateFilter derivate, ITimeSeriesDistance distance) {
        this(alpha, derivate, distance, distance);
    }

    /**
     * Constructor that uses the same distance measures for the function and
     * derivate values that uses the {@link BackwardDifferenceDerivate} as
     * derivation.
     * 
     * @param alpha    The distance measure to use to calculate the distance of the
     *                 function values. <code>0 <= alpha <= pi/2</code>.
     * @param distance The distance measure to use to calculate the distance of the
     *                 function and derivate values.
     */
    public DerivateDistance(double alpha, ITimeSeriesDistance distance) {
        this(alpha, new BackwardDifferenceDerivate(), distance, distance);
    }

    @Override
    public double distance(double[] A, double[] B) {
        double[] derivateA = this.derivate.transform(A);
        double[] derivateB = this.derivate.transform(B);

        return this.a * timeSeriesDistance.distance(A, B) + this.b * derivateDistance.distance(derivateA, derivateB);
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