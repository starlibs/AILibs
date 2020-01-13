package org.api4.java.common.metric;

import org.api4.java.common.math.IMetric;

/**
 * Functional interface for the distance of two scalars.
 *
 * @author fischor
 */
public interface IScalarDistance extends IMetric<Double> {
	default double distance(final double a, final double b) {
		return this.getDistance(a, b);
	}
}