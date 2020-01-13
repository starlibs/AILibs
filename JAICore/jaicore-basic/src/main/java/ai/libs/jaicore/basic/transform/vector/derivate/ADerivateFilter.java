package ai.libs.jaicore.basic.transform.vector.derivate;

import ai.libs.jaicore.basic.transform.vector.IVectorTransform;

/**
 * Abstract superclass for all derivate filters.
 *
 * @author fischor
 */
public abstract class ADerivateFilter implements IVectorTransform {

	/**
	 * Flag that states wheter the filter should add a padding to the derivate
	 * assure that is has the same length as the origin time series or not.
	 */
	protected boolean withBoundaries;

	public ADerivateFilter(final boolean withBoundaries) {
		this.withBoundaries = withBoundaries;
	}

	public ADerivateFilter() {
		this.withBoundaries = false;
	}

	/**
	 * Calculates the derivate of a time series.
	 *
	 * @param t The time series to calculate the derivate for.
	 * @return The derivate of the time series.
	 */
	protected abstract double[] derivate(double[] t);

	/**
	 * Calcuates the derivates of a time series. In contrast to the normal
	 * {@link derivate} calculation, this method is guaranteed to return a derivate
	 * that has the same length than the original time series. This is accomplished
	 * via padding.
	 *
	 * @param t The time series to calculate the derivate for.
	 * @return The, possibly padded, derivate of the time series.
	 */
	protected abstract double[] derivateWithBoundaries(double[] t);

	@Override
	public double[] transform(final double[] input) {
		if (this.withBoundaries) {
			return this.derivateWithBoundaries(input);
		} else {
			return this.derivate(input);
		}
	}

}