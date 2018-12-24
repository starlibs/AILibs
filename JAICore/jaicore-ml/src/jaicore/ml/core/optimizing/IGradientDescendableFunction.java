package jaicore.ml.core.optimizing;

import de.upb.isys.linearalgebra.Vector;

/**
 * This interface represents a function that is differentiable and thus can be
 * used by gradient descent algorithms.
 * 
 * In particular, if a function implements this interface, we assume that the
 * underlying space is convex.
 * 
 * @author Helena Graf, Mirko Jürgens
 *
 */
public interface IGradientDescendableFunction {

	/**
	 * Applies the function for the point represented by the given vector.
	 * 
	 * @param vector
	 *            the point to which to apply the function
	 * @return the function value at the applied point
	 */
	double apply(Vector vector);
}
