package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

import ai.libs.jaicore.basic.transform.vector.CosineTransform;
import ai.libs.jaicore.basic.transform.vector.IVectorTransform;
import ai.libs.jaicore.basic.transform.vector.derivate.ADerivateFilter;
import ai.libs.jaicore.basic.transform.vector.derivate.BackwardDifferenceDerivate;

/**
 * Implementation of the Derivate Transform Distance (TD) measure as published
 * in "Non-isometric transforms in time series classification using DTW" by
 * Tomasz Gorecki and Maciej Luczak (2014).
 *
 * As the name suggests, with the Derivate Transform Distance the author combine
 * their previously approaches of @see DerivateDistance and @see
 * TransformDistance.
 *
 * Given a distance measure <code>d</code>, the Derivate Transform Distance for
 * the two time series <code>A</code> and <code>B</code> is:
 * <code>a * d(A, B) + b * d(A', B') + c * d(t(A), t(B))</code>, where
 * <code>A'</code> and <code>B'</code> are the derivates (@see
 * jaicore.ml.tsc.filter.derivate) and <code>t(A)</code> and <code>t(B)</code>
 * are transforms (@see jaicore.ml.tsc.filter.transform) of <code>A</code> and
 * <code>B</code> respec. and
 * <code>0 <= a <= 1, 0 <= b <= 1, 0 <= c <= 1></code> are parameters of the
 * measure.
 *
 * The Derivate Transform Distance that uses Dynamic Time Warping as underlying
 * distance measure is commonly denoted as DTD_DTW. The Derivate Transform
 * Distance that uses the Euclidean distance as underlying distance measure is
 * commonly denoted as TD_ED.
 * <p>
 * It is also possible to use a distinct distance measure to calculate the
 * distance between the time series, its transforms and its derivates.
 * </p>
 *
 * @author fischor
 */
public class DerivateTransformDistance implements IDistanceMetric {

	/**
	 * Determines the influence of distance of the function values to the overall
	 * distance measure.
	 */
	private double a;

	/**
	 * Determines the influence of distance of the derivate values to the overall
	 * distance measure.
	 */
	private double b;

	/**
	 * Determines the influence of distance of the transform values to the overall
	 * distance measure.
	 */
	private double c;

	/** The derivate calculation to use. */
	private ADerivateFilter derivate;

	/** The transform calculation to use. */
	private IVectorTransform transform;

	/**
	 * The distance measure to use to calculate the distance of the function values.
	 */
	private IDistanceMetric timeSeriesDistance;

	/**
	 * The distance measure to use to calculate the distance of the derivate values.
	 */
	private IDistanceMetric derivateDistance;

	/**
	 * The distance measure to use to calculate the distance of the transform
	 * values.
	 */
	private IDistanceMetric transformDistance;

	/**
	 * Constructor with individual distance measure for function, derivate and
	 * transform values.
	 *
	 * @param a Determines the influence of distance of the
	 *            derivate values to the overall distance measure.
	 * @param b Determines the influence of distance of the
	 *            transform values to the overall distance measure.
	 * @param c Determines the influence of distance of the
	 *            transform values to the overall distance measure.
	 * @param derivate The derivate calculation to use.
	 * @param transform The transform calculation to use.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the function values.
	 * @param derivateDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 * @param transformDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 */
	public DerivateTransformDistance(final double a, final double b, final double c, final ADerivateFilter derivate, final IVectorTransform transform, final IDistanceMetric timeSeriesDistance, final IDistanceMetric derivateDistance,
			final IDistanceMetric transformDistance) {
		// Parameter checks.
		if (derivate == null) {
			throw new IllegalArgumentException("Parameter derivate must not be null.");
		}
		if (transform == null) {
			throw new IllegalArgumentException("Parameter transform must not be null.");
		}
		if (timeSeriesDistance == null) {
			throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
		}
		if (derivateDistance == null) {
			throw new IllegalArgumentException("Parameter derivateDistance must not be null.");
		}
		if (transformDistance == null) {
			throw new IllegalArgumentException("Parameter transformDistance must not be null.");
		}
		this.setA(a);
		this.setB(b);
		this.setC(c);
		this.derivate = derivate;
		this.transform = transform;
		this.timeSeriesDistance = timeSeriesDistance;
		this.derivateDistance = derivateDistance;
		this.transformDistance = transformDistance;
	}

	/**
	 * Constructor with individual distance measure for function, derivate and
	 * transform values that uses the {@link BackwardDifferencetransform} as
	 * derivate and the {@link CosineTransform} as transformation.
	 *
	 * @param a Determines the influence of distance of the
	 *            derivate values to the overall distance measure.
	 * @param b Determines the influence of distance of the
	 *            transform values to the overall distance measure.
	 * @param c Determines the influence of distance of the
	 *            transform values to the overall distance measure.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the function values.
	 * @param derivateDistance The distance measure to use to calculate the
	 *            distance of the derivate values.
	 * @param transformDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 */
	public DerivateTransformDistance(final double a, final double b, final double c, final IDistanceMetric timeSeriesDistance, final IDistanceMetric derivateDistance, final IDistanceMetric transformDistance) {
		this(a, b, c, new BackwardDifferenceDerivate(), new CosineTransform(), timeSeriesDistance, derivateDistance, transformDistance);
	}

	/**
	 * Constructor that uses the same distance measures for function, derivate and
	 * transform values.
	 *
	 * @param a Determines the influence of distance of the derivate values
	 *            to the overall distance measure.
	 * @param b Determines the influence of distance of the transform values
	 *            to the overall distance measure.
	 * @param c Determines the influence of distance of the transform values
	 *            to the overall distance measure.
	 * @param derivate The derivate calculation to use.
	 * @param transform The transform calculation to use.
	 * @param distance The distance measure to use of the function, derivate and
	 *            transform values.
	 */
	public DerivateTransformDistance(final double a, final double b, final double c, final ADerivateFilter derivate, final IVectorTransform transform, final IDistanceMetric distance) {
		this(a, b, c, derivate, transform, distance, distance, distance);
	}

	/**
	 * Constructor that uses the same distance measures for function, derivate and
	 * transform values.
	 *
	 * @param a Determines the influence of distance of the derivate values
	 *            to the overall distance measure.
	 * @param b Determines the influence of distance of the transform values
	 *            to the overall distance measure.
	 * @param c Determines the influence of distance of the transform values
	 *            to the overall distance measure.
	 * @param distance The distance measure to use of the function, derivate and
	 *            transform values.
	 */
	public DerivateTransformDistance(final double a, final double b, final double c, final IDistanceMetric distance) {
		this(a, b, c, new BackwardDifferenceDerivate(), new CosineTransform(), distance, distance, distance);
	}

	@Override
	public double distance(final double[] a, final double[] b) {
		double[] derivateA = this.derivate.transform(a);
		double[] derivateB = this.derivate.transform(b);
		double[] transformA = this.transform.transform(a);
		double[] transformB = this.transform.transform(b);

		return this.a * this.timeSeriesDistance.distance(a, b) + this.b * this.derivateDistance.distance(derivateA, derivateB) + this.c * this.transformDistance.distance(transformA, transformB);
	}

	/**
	 * Sets the <code>a</code> parameter. @see #a
	 *
	 * @param a The <code>a</code> parameter, <code>0 <= a <= 1</code>.
	 */
	public void setA(final double a) {
		if (a < 0 || a > 1) {
			throw new IllegalArgumentException("Parameter a must be in interval [0,1].");
		}
		this.a = a;
	}

	/**
	 * Sets the <code>b</code> parameter. @see #b
	 *
	 * @param a The <code>b</code> parameter, <code>0 <= b <= 1</code>.
	 */
	public void setB(final double b) {
		if (b < 0 || b > 1) {
			throw new IllegalArgumentException("Parameter b must be in interval [0,1].");
		}
		this.b = b;
	}

	/**
	 * Sets the <code>c</code> parameter. @see #c
	 *
	 * @param a The <code>c</code> parameter, <code>0 <= c <= 1</code>.
	 */
	public void setC(final double c) {
		if (c < 0 || c > 1) {
			throw new IllegalArgumentException("Parameter c must be in interval [0,1].");
		}
		this.c = c;
	}

	/**
	 * Getter for the <code>a</code> parameter. @see #a
	 */
	public double getA() {
		return this.a;
	}

	/**
	 * Getter for the <code>b</code> parameter. @see #b
	 */
	public double getB() {
		return this.b;
	}

	/**
	 * Getter for the <code>c</code> parameter. @see #c
	 */
	public double getC() {
		return this.c;
	}

}