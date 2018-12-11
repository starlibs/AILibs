package jaicore.ml.dyadranking.algorithm.lbfgs;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.optimizing.IGradientBasedOptimizer;
import jaicore.ml.dyadranking.optimizing.IGradientDescendableFunction;
import jaicore.ml.dyadranking.optimizing.IGradientFunction;

public class LBFGSOptimizerWrapper implements IGradientBasedOptimizer {

	private static final Logger log = LoggerFactory.getLogger(LBFGSOptimizerWrapper.class);

	@Override
	public Vector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient,
			Vector initialGuess) {
		/** Workaround for solving argmin */
		double [] coeffs = initialGuess.asArray();
		log.debug("Got optimization request.");
		LBFGS.Result optimizedResult = LBFGS.lbfgs(coeffs,
				(double[] x, double[] gradientToFill, int numParams, double stepSize) -> {
					Vector inputVector = new DenseDoubleVector(x);
					double result = descendableFunction.apply(inputVector);
					Vector actualGradient = gradient.apply(inputVector);
					if (actualGradient.length() != gradientToFill.length) {
						throw new IllegalStateException(
								"Length mismatch! The length of the expected gradient does not match the length of the actual gradient.");
					}
					for (int i = 0; i < gradientToFill.length; i++) {
						gradientToFill[i] = actualGradient.asArray()[i];
					}
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
