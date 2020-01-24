package ai.libs.jaicore.basic.metric;

import org.api4.java.common.metric.IDistanceMetric;

public abstract class AWeightedTrigometricDistance implements IDistanceMetric {

	/**
	 * Alpha value that adjusts the parameters {@link a} and {@link b}, that is
	 * <code>a=cos(alpha)</code> and <code>b=sin(alpha)</code>. It holds, that
	 * <code>0 <= alpha <= pi/2</code>.
	 */
	private double alpha;
	private double a;
	private double b;

	public AWeightedTrigometricDistance(final double alpha) {
		super();
		// Parameter checks.
		if (alpha > Math.PI / 2 || alpha < 0) {
			throw new IllegalArgumentException("Parameter alpha has to be between 0 (inclusive) and pi/2 (inclusive).");
		}
		this.setAlpha(alpha);
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
