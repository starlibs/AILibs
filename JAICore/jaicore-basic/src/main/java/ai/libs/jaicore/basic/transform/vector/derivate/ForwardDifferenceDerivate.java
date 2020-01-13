package ai.libs.jaicore.basic.transform.vector.derivate;

/**
 * Filter that calculate the Forward Difference derivate. The Forward Difference
 * derivate <code>T'</code> for a time series
 * <code>T = {T(0), T(1), T(2), .., T(n)}<code> is defined as <code>T'(i) = T(i+1) - T(i)</code>
 * for <code>i = 0 to n-1</code>. When padded, <code>T'(n) = T'(n-1)</code>.
 *
 * @author fischor
 */
public class ForwardDifferenceDerivate extends ADerivateFilter {

	public ForwardDifferenceDerivate() {
		super();
	}

	public ForwardDifferenceDerivate(final boolean withBoundaries) {
		super(withBoundaries);
	}

	@Override
	protected double[] derivate(final double[] t) {
		double[] derivate = new double[t.length - 1];

		for (int i = 0; i < t.length - 1; i++) {
			derivate[i] = t[i + 1] - t[i];
		}

		return derivate;
	}

	@Override
	protected double[] derivateWithBoundaries(final double[] t) {
		double[] derivate = new double[t.length];

		for (int i = 0; i < t.length - 1; i++) {
			derivate[i] = t[i + 1] - t[i];
		}

		derivate[t.length - 1] = derivate[t.length - 2];
		return derivate;
	}
}