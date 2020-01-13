package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

import ai.libs.jaicore.basic.transform.vector.derivate.ADerivateFilter;
import ai.libs.jaicore.basic.transform.vector.derivate.BackwardDifferenceDerivate;

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
 *
 * @author fischor
 */
public class DerivateDistance extends AWeightedTrigometricDistance {

	/** The derivate calculation to use. */
	private ADerivateFilter derivate;

	/**
	 * The distance measure to use to calculate the distance of the function values.
	 */
	private IDistanceMetric timeSeriesDistance;

	/**
	 * The distance measure to use to calculate the distance of the derivate values.
	 */
	private IDistanceMetric baseDerivateDistance;

	/**
	 * Constructor with individual distance measures for the function and derivate
	 * values.
	 *
	 * @param alpha The distance measure to use to calculate the
	 *            distance of the function values.
	 *            <code>0 <= alpha <= pi/2</code>.
	 * @param derivate The derivate calculation to use.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 * @param derivateDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 */
	public DerivateDistance(final double alpha, final ADerivateFilter derivate, final IDistanceMetric timeSeriesDistance, final IDistanceMetric derivateDistance) {
		super(alpha);
		if (derivate == null) {
			throw new IllegalArgumentException("Parameter derivate must not be null.");
		}
		if (timeSeriesDistance == null) {
			throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
		}
		if (derivateDistance == null) {
			throw new IllegalArgumentException("Parameter derivateDistance must not be null.");
		}
		this.derivate = derivate;
		this.timeSeriesDistance = timeSeriesDistance;
		this.baseDerivateDistance = derivateDistance;
	}

	/**
	 * Constructor with individual distance measures for the function and derivate
	 * values that uses the {@link BackwardDifferenceDerivate} as derivation.
	 *
	 * @param alpha The distance measure to use to calculate the
	 *            distance of the function values.
	 *            <code>0 <= alpha <= pi/2</code>.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 * @param derivateDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 */
	public DerivateDistance(final double alpha, final IDistanceMetric timeSeriesDistance, final IDistanceMetric derivateDistance) {
		this(alpha, new BackwardDifferenceDerivate(), timeSeriesDistance, derivateDistance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * derivate values.
	 *
	 * @param alpha The distance measure to use to calculate the distance of the
	 *            function values. <code>0 <= alpha <= pi/2</code>.
	 * @param derivate The derivate calculation to use.
	 * @param distance The distance measure to use to calculate the distance of the
	 *            function and derivate values.
	 */
	public DerivateDistance(final double alpha, final ADerivateFilter derivate, final IDistanceMetric distance) {
		this(alpha, derivate, distance, distance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * derivate values that uses the {@link BackwardDifferenceDerivate} as
	 * derivation.
	 *
	 * @param alpha The distance measure to use to calculate the distance of the
	 *            function values. <code>0 <= alpha <= pi/2</code>.
	 * @param distance The distance measure to use to calculate the distance of the
	 *            function and derivate values.
	 */
	public DerivateDistance(final double alpha, final IDistanceMetric distance) {
		this(alpha, new BackwardDifferenceDerivate(), distance, distance);
	}

	/**
	 * @param a The influence of distance of the function values to the overall distance measure
	 * @param b The influence of distance of the derivates values to the overall distance measure.
	 */
	@Override
	public double distance(final double[] a, final double[] b) {
		double[] derivateA = this.derivate.transform(a);
		double[] derivateB = this.derivate.transform(b);

		return this.getA() * this.timeSeriesDistance.distance(a, b) + this.getB() * this.baseDerivateDistance.distance(derivateA, derivateB);
	}
}