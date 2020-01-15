package ai.libs.jaicore.math.gradientdescent;

import org.api4.java.common.math.IVector;

/**
 * Interface for an optimizer that is based on a gradient descent and gets a
 * differentiable function and the derivation of said function to solve an
 * optimization problem.
 *
 * @author Helena Graf, Mirko J�rgens
 *
 */
public interface IGradientBasedOptimizer {

	/**
	 * Optimize the given function based on its derivation.
	 *
	 * @param descendableFunction
	 *            the function to optimize
	 * @param gradient
	 *            the first order derivate of the function
	 * @param initialGuess
	 *            the initial guess for the parameters that shall be optimized
	 * @return the optimized vector
	 */
	public IVector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient, IVector initialGuess);
}
