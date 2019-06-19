package ai.libs.jaicore.ml.core.optimizing;

import ai.libs.jaicore.math.linearalgebra.Vector;

/**
 * Represents the gradient of a function that is differentiable.
 *
 * @author Helena Graf, Mirko Jürgens
 *
 */
public interface IGradientFunction {

	/**
	 * Returns the result of applying the gradient to the point represented by the
	 * given vector.
	 *
	 * @param vector
	 *            the vector the gradient is applied to
	 * @return the new vector resulting from applying the gradient to the given
	 *         vector
	 */
	Vector apply(Vector vector);
}
