package ai.libs.jaicore.math.gradientdescent;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.common.math.IVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An optimizer based on the gradient descent method [1]. This optimizer is the
 * naive implementation that calculates the gradient in every step and makes an
 * update into the negative direction of the gradient.
 *
 * This method is known to find the optimum, if the underlying function is convex.
 *
 * At some point in the future, we should probably implement faster methods,
 * like for example http://www.seas.ucla.edu/~vandenbe/236C/lectures/fgrad.pdf
 *
 * [1] Jonathan Barzilai and Jonathan M. Borwein, "Two-point step size gradient
 * methods", in: IMA journal of numerical analysis, 8.1 (1998), pp. 141-148.
 *
 * @author Mirko J�rgens
 *
 */
public class GradientDescentOptimizer implements IGradientBasedOptimizer {

	private double learningRate;

	private final double gradientThreshold;

	private final int maxIterations;

	private static final Logger log = LoggerFactory.getLogger(GradientDescentOptimizer.class);

	/**
	 *
	 * @param config
	 */
	public GradientDescentOptimizer(final IGradientDescentOptimizerConfig config) {
		this.learningRate = config.learningRate();
		this.gradientThreshold = config.gradientThreshold();
		this.maxIterations = config.maxIterations();
	}

	public GradientDescentOptimizer() {
		this(ConfigFactory.create(IGradientDescentOptimizerConfig.class));
	}

	@Override
	public IVector optimize(final IGradientDescendableFunction descendableFunction, final IGradientFunction gradient, final IVector initialGuess) {
		int iterations = 0;
		IVector gradients;
		do {
			gradients = gradient.apply(initialGuess);
			iterations++;
			this.updatePredictions(initialGuess, gradients);
			log.warn("iteration {}:\n weights \t{} \n gradients \t{}", iterations, initialGuess, gradients);

		} while (!this.allGradientsAreBelowThreshold(gradients) && iterations < this.maxIterations);
		log.warn("Gradient descent based optimization took {} iterations.", iterations);
		return initialGuess;
	}

	private boolean allGradientsAreBelowThreshold(final IVector gradients) {
		return gradients.stream().allMatch(grad -> Math.abs(grad) < this.gradientThreshold || !Double.isFinite(grad));
	}

	private void updatePredictions(final IVector initialGuess, final IVector gradients) {
		for (int i = 0; i < initialGuess.length(); i++) {
			double weight = initialGuess.getValue(i);
			double gradient = gradients.getValue(i);
			// don't further optimize if we meet the threshold
			if (Math.abs(gradient) < this.gradientThreshold) {
				continue;
			}
			// we want to minimize
			gradient = gradient * -1.0;
			weight = weight + gradient * this.learningRate;
			initialGuess.setValue(i, weight);
		}

	}

}
