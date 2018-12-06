package jaicore.ml.dyadranking.optimizing;

import de.upb.isys.linearalgebra.Vector;

/**
 * Interface for an optimizer that is based on a gradient descent and gets a
 * differentiable function and the derivation of said function to solve an
 * optimization problem.
 * 
 * @author Helena Graf
 *
 */
public interface IGradientBasedOptimizer {

	/**
	 * Optimize the given function based on its derivation.
	 * 
	 * @param descendableFunction
	 *            the function to optimize
	 * @param gradient
	 *            the derivation of the function
	 * @return the optimized vector
	 */
	public Vector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient);
}
