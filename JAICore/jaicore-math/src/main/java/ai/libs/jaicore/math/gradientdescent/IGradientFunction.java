package ai.libs.jaicore.math.gradientdescent;

import ai.libs.jaicore.math.linearalgebra.IVector;

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
	IVector apply(IVector vector);
}
