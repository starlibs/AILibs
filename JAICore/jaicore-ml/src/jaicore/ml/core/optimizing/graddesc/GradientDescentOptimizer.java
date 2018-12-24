package jaicore.ml.core.optimizing.graddesc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.core.optimizing.IGradientBasedOptimizer;
import jaicore.ml.core.optimizing.IGradientDescendableFunction;
import jaicore.ml.core.optimizing.IGradientFunction;

/**
 * An optimizer based on the gradient descent method [1].
 * 
 * @author Mirko Jürgens
 *
 */
public class GradientDescentOptimizer implements IGradientBasedOptimizer {

	private static float LEARNING_RATE = 0.008f;

	private static final double GRADIENT_THRESHHOLD = 1e-3;
	
	private static final int MAX_ITERATIONS = 30;

	private static final Logger log = LoggerFactory.getLogger(GradientDescentOptimizer.class);

	@Override
	public Vector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient,
			Vector initialGuess) {
		int iterations = 0;
		Vector gradients;
		do {
			if (iterations % 10 == 0)
				LEARNING_RATE = LEARNING_RATE / 2;
			gradients = gradient.apply(initialGuess);
			iterations++;
			updatePredictions(initialGuess, gradients);
			log.warn("iteration {}; w{} and g {}", iterations, initialGuess, gradients);
			
		} while (!allGradientsAreBelowThreshold(gradients) && iterations < MAX_ITERATIONS);
		log.warn("Gradient descent based optimization took {} iterations.", iterations);
		return initialGuess;
	}

	private boolean allGradientsAreBelowThreshold(Vector gradients) {
		return gradients.stream().allMatch(grad -> Math.abs(grad) < GRADIENT_THRESHHOLD || !Double.isFinite(grad));
	}

	private void updatePredictions(Vector initialGuess, Vector gradients) {
		for (int i = 0; i < initialGuess.length(); i++) {
			float weight = (float) initialGuess.getValue(i);
			float gradient = (float) gradients.getValue(i);
			// don't further optimize if we me the threshold
			if (Math.abs(gradient) < GRADIENT_THRESHHOLD)
				continue;
			// we want to minimize
			gradient = gradient * -1f;
			weight = weight + gradient * LEARNING_RATE;
			initialGuess.setValue(i, weight);
		}

	}

}
