package jaicore.ml.core.optimizing.lbfgs;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.optimizing.IGradientBasedOptimizer;
import jaicore.ml.core.optimizing.IGradientDescendableFunction;
import jaicore.ml.core.optimizing.IGradientFunction;

/**
 * Wraps the LBFGS class to our optimizer interface. The LBFGS optimizer
 * requires us to fill the gradient in a functional interface, we wrapped this
 * to our gradient call and copy the gradient.
 * 
 * @author Mirko
 *
 */
public class LBFGSOptimizerWrapper implements IGradientBasedOptimizer {

	private static final Logger log = LoggerFactory.getLogger(LBFGSOptimizerWrapper.class);

	@Override
	public Vector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient,
			Vector initialGuess) {
		/*We store the coeffs array as the optimizer will constantly update it with the latest guess. */
		double[] coeffs = initialGuess.asArray();
		log.debug("Got optimization request.");
		LBFGS.Result optimizedResult = LBFGS.lbfgs(coeffs,
				(double[] x, double[] gradientToFill, int numParams, double stepSize) -> {
					// x is the current step of the gradient
					Vector inputVector = new DenseDoubleVector(x);
					// f(x)
					double result = descendableFunction.apply(inputVector);
					//copy the gradient to the provided gradient array
					Vector actualGradient = gradient.apply(inputVector);
					if (actualGradient.length() != gradientToFill.length) {
						throw new IllegalStateException(
								"Length mismatch! The length of the expected gradient does not match the length of the actual gradient.");
					}
					for (int i = 0; i < gradientToFill.length; i++) {
						gradientToFill[i] = actualGradient.asArray()[i];
					}
					log.debug("Current Gradient is {}", actualGradient);
					//return f(x)
					return result;
				});
		log.debug("Optimization finished. The wrapped optimizer returned status {}", optimizedResult.status);
		if (optimizedResult.status != LBFGS.Status.LBFGS_SUCCESS && optimizedResult.status != LBFGS.Status.LBFGS_STOP) {
			log.warn("LBFGS returned no success, the result may not be the optimial result!");
		}
		log.debug("LBFGS returned {}", Arrays.toString(coeffs));
		return new DenseDoubleVector(coeffs);
	}

}
