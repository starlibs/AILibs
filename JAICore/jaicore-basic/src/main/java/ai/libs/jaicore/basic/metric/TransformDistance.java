package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

import ai.libs.jaicore.basic.transform.vector.CosineTransform;
import ai.libs.jaicore.basic.transform.vector.IVectorTransform;

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
public class TransformDistance extends AWeightedTrigometricDistance {

	/** The transform calculation to use. */
	private IVectorTransform transform;

	/**
	 * The distance measure to use to calculate the distance of the function values.
	 */
	private IDistanceMetric timeSeriesDistance;

	/**
	 * The distance measure to use to calculate the distance of the transform
	 * values.
	 */
	private IDistanceMetric baseTransformDistance;

	/**
	 * Constructor with individual distance measures for the function and transform
	 * values.
	 *
	 * @param alpha @see #alpha ,<code>0 <= alpha <= pi/2</code>.
	 * @param transform The transform calculation to use.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 * @param transformDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 */
	public TransformDistance(final double alpha, final IVectorTransform transform, final IDistanceMetric timeSeriesDistance, final IDistanceMetric transformDistance) {
		super(alpha);
		if (transform == null) {
			throw new IllegalArgumentException("Parameter transform must not be null.");
		}
		if (timeSeriesDistance == null) {
			throw new IllegalArgumentException("Parameter timeSeriesDistance must not be null.");
		}
		if (transformDistance == null) {
			throw new IllegalArgumentException("Parameter transformDistance must not be null.");
		}
		this.transform = transform;
		this.timeSeriesDistance = timeSeriesDistance;
		this.baseTransformDistance = transformDistance;
	}

	/**
	 * Constructor with individual distance measures for the function and transform
	 * values that uses the {@link CosineTransform} as transformation.
	 *
	 * @param alpha The distance measure to use to calculate the
	 *            distance of the function values.
	 *            <code>0 <= alpha <= pi/2</code>.
	 * @param timeSeriesDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 * @param transformDistance The distance measure to use to calculate the
	 *            distance of the transform values.
	 */
	public TransformDistance(final double alpha, final IDistanceMetric timeSeriesDistance, final IDistanceMetric transformDistance) {
		this(alpha, new CosineTransform(), timeSeriesDistance, transformDistance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * transform values.
	 *
	 * @param alpha The distance measure to use to calculate the distance of the
	 *            function values. <code>0 <= alpha <= pi/2</code>.
	 * @param transform The transform calculation to use.
	 * @param distance The distance measure to use to calculate the distance of the
	 *            function and transform values.
	 */
	public TransformDistance(final double alpha, final IVectorTransform transform, final IDistanceMetric distance) {
		this(alpha, transform, distance, distance);
	}

	/**
	 * Constructor that uses the same distance measures for the function and
	 * transform values that uses the {@link CosineTransform} as transformation.
	 *
	 * @param alpha The distance measure to use to calculate the distance of the
	 *            function values. <code>0 <= alpha <= pi/2</code>.
	 * @param distance The distance measure to use to calculate the distance of the
	 *            function and transform values.
	 */
	public TransformDistance(final double alpha, final IDistanceMetric distance) {
		this(alpha, new CosineTransform(), distance);
	}

	/**
	 * @param a The influence of distance of the function values to the overall distance measure.
	 * @param b The influence of distance of the transform values to the overall distance measure.
	 *
	 */
	@Override
	public double distance(final double[] a, final double[] b) {
		double[] transformA = this.transform.transform(a);
		double[] transformB = this.transform.transform(b);
		return this.getA() * this.timeSeriesDistance.distance(a, b) + this.getB() * this.baseTransformDistance.distance(transformA, transformB);
	}
}