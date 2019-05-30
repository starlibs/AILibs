package jaicore.ml.tsc.distances;

import jaicore.ml.tsc.filter.transform.ATransformFilter;
import jaicore.ml.tsc.filter.transform.CosineTransform;

/**
 * Implementation of the Transform Distance (TD) measure as published in
 * "Non-isometric transforms in time series classification using DTW" by Tomasz
 * Gorecki and Maciej Luczak (2014).
 *
 * Building up on their work of the Derivate Distance, the authors were looking
 * for functions other than the derivative which can be used in a similar
 * manner, what leads them to use transform instead of derivates.
 *
 * Given a distance measure <code>d</code>, the Transform Distance for the two
 * time series <code>A</code> and <code>B</code> is:
 * <code>a * d(A, B) + b * d(t(A), t(B))</code>, where <code>t(A)</code> and
 * <code>t(B)</code> are transforms (@see jaicore.ml.tsc.filter.transform) of
 * <code>A</code> and <code>B</code> respec. and
 * <code>0 <= a <= 1, 0 <= b <= 1></code> are parameters of the measure. The
 * parameters <code>a</code> and <code>b</code> are set via an
 * <code>alpha</code> value, that is <code>a=cos(alpha)</code> and
 * <code>b=sin(alpha)</code>.
 *
 * The Transform Distance that uses Dynamic Time Warping as underlying distance
 * measure is commonly denoted as TD_DTW. The Transform Distance that uses the
 * Euclidean distance as underlying distance measure is commonly denoted as
 * TD_ED.
 * <p>
 * It is also possible to use a distinct distance measure to calculate the
 * distance between the time series and its transforms.
 * </p>
 *
 * @author fischor
 */
public class TransformDistance implements ITimeSeriesDistance {

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
	 * Determines the influence of distance of the transform values to the overall
	 * distance measure.
	 */
	private double b;

	/** The transform calculation to use. */
	private ATransformFilter transform;

	/**
	 * The distance measure to use to calculate the distance of the function values.
	 */
	private ITimeSeriesDistance timeSeriesDistance;

	/**
	 * The distance measure to use to calculate the distance of the transform
	 * values.
	 */
	private ITimeSeriesDistance transformDistance;

	/**
	 * Constructor with individual distance measures for the function and transform
	 * values.
	 *
	 * @param alpha              @see #alpha ,<code>0 <= alpha <= pi/2</code>.
	 * @param transform          The transform calculation to use.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *                           distance of the transform values.
	 * @param transformDistance  The distance measure to use to calculate the
	 *                           distance of the transform values.
	 */
	public TransformDistance(final double alpha, final ATransformFilter transform, final ITimeSeriesDistance timeSeriesDistance,
			final ITimeSeriesDistance transformDistance) {
		// Parameter checks.
		if (alpha > Math.PI / 2 || alpha < 0) {
			throw new IllegalArgumentException("Parameter alpha has to be between 0 (inclusive) and pi/2 (inclusive).");
		}
		if (transform == null) {
			throw new IllegalArgumentException("Parameter transform must not be null.");
		}
		if (timeSeriesDistance == null) {
			throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
		}
		if (transformDistance == null) {
			throw new IllegalArgumentException("Parameter transformDistance must not be null.");
		}

		this.setAlpha(alpha);
		this.transform = transform;
		this.timeSeriesDistance = timeSeriesDistance;
		this.transformDistance = transformDistance;
	}

	/**
	 * Constructor with individual distance measures for the function and transform
	 * values that uses the {@link CosineTransform} as transformation.
	 *
	 * @param alpha              The distance measure to use to calculate the
	 *                           distance of the function values.
	 *                           <code>0 <= alpha <= pi/2</code>.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *                           distance of the transform values.
	 * @param transformDistance  The distance measure to use to calculate the
	 *                           distance of the transform values.
	 */
	public TransformDistance(final double alpha, final ITimeSeriesDistance timeSeriesDistance,
			final ITimeSeriesDistance transformDistance) {
		this(alpha, new CosineTransform(), timeSeriesDistance, transformDistance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * transform values.
	 *
	 * @param alpha     The distance measure to use to calculate the distance of the
	 *                  function values. <code>0 <= alpha <= pi/2</code>.
	 * @param transform The transform calculation to use.
	 * @param distance  The distance measure to use to calculate the distance of the
	 *                  function and transform values.
	 */
	public TransformDistance(final double alpha, final ATransformFilter transform, final ITimeSeriesDistance distance) {
		this(alpha, transform, distance, distance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * transform values that uses the {@link CosineTransform} as transformation.
	 *
	 * @param alpha    The distance measure to use to calculate the distance of the
	 *                 function values. <code>0 <= alpha <= pi/2</code>.
	 * @param distance The distance measure to use to calculate the distance of the
	 *                 function and transform values.
	 */
	public TransformDistance(final double alpha, final ITimeSeriesDistance distance) {
		this(alpha, new CosineTransform(), distance);
	}

	@Override
	public double distance(final double[] a, final double[] b) {
		double[] transformA = this.transform.transform(a);
		double[] transformB = this.transform.transform(b);

		return this.a * this.timeSeriesDistance.distance(a, b) + this.b * this.transformDistance.distance(transformA, transformB);
	}

	/**
	 * Sets the alpha value and adjusts the measurement parameters
	 * <code>a = cos(alpha)<code> and <code>b = sin(alpha)</code> accordingly.
	 *
	 * @param alpha The alpha value, <code>0 <= alpha <= pi/2</code>.
	 */
	public void setAlpha(final double alpha) {
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
		return this.alpha;
	}

	/**
	 * Getter for the <code>a</code> parameter. @see #a
	 */
	public double getA() {
		return this.a;
	}

	/**
	 * Getter for the <code>a</code> parameter. @see #b
	 */
	public double getB() {
		return this.b;
	}

}