package jaicore.ml.core.optimizing.graddesc;

import org.aeonbits.owner.ConfigFactory;
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

	private final double learningRate;

	private final double gradientThreshold;

	private final int maxIterations;

	private static final Logger log = LoggerFactory.getLogger(GradientDescentOptimizer.class);

	public GradientDescentOptimizer(GradientDescentOptimizerConfig config) {
		this.learningRate = config.learningRate();
		this.gradientThreshold = config.gradientThreshold();
		this.maxIterations = config.maxIterations();
	}

	public GradientDescentOptimizer() {
		this(ConfigFactory.create(GradientDescentOptimizerConfig.class));
	}

	@Override
	public Vector optimize(IGradientDescendableFunction descendableFunction, IGradientFunction gradient,
			Vector initialGuess) {
		int iterations = 0;
		Vector gradients;
		do {
			gradients = gradient.apply(initialGuess);
			iterations++;
			updatePredictions(initialGuess, gradients);
			log.warn("iteration {}:\n weights \t{} \n gradients \t{}", iterations, initialGuess, gradients);

		} while (!allGradientsAreBelowThreshold(gradients) && iterations < maxIterations);
		log.warn("Gradient descent based optimization took {} iterations.", iterations);
		return initialGuess;
	}

	private boolean allGradientsAreBelowThreshold(Vector gradients) {
		return gradients.stream().allMatch(grad -> Math.abs(grad) < gradientThreshold || !Double.isFinite(grad));
	}

	private void updatePredictions(Vector initialGuess, Vector gradients) {
		for (int i = 0; i < initialGuess.length(); i++) {
			double weight = initialGuess.getValue(i);
			double gradient = gradients.getValue(i);
			// don't further optimize if we meet the threshold
			if (Math.abs(gradient) < gradientThreshold)
				continue;
			// we want to minimize
			gradient = gradient * -1.0;
			weight = weight + gradient * learningRate;
			initialGuess.setValue(i, weight);
		}

	}

}
