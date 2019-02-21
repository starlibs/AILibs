package jaicore.ml.core.optimizing.graddesc;

import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;

@Sources({ "file:conf/opt/graddesc.properties" })
public interface GradientDescentOptimizerConfig extends Mutable{

	/**
	 * Specifies the maximum of gradient update steps. Can be set to -1 to specify
	 * no threshold (the algorithm may not terminate in this case).
	 */
	public static final String GRAD_DESC_MAX_ITERATIONS = "graddesc.max_iterations";

	/**
	 * The learning rate in the update step (i.e. how much of the gradient should be
	 * added to the parameter)
	 */
	public static final String GRAD_DESC_LEARNING_RATE = "gradedesc.learning_rate";

	/**
	 * Specifies a threshold for the gradient (i.e. if the gradient is below this
	 * value no update will be done; if all gradients are below this value, the
	 * algorithm will terminate)
	 */
	public static final String GRAD_DESC_GRADIENT_THRESHOLD = "graddesc.gradient_threshold";

	@Key(GRAD_DESC_MAX_ITERATIONS)
	@DefaultValue("20")
	public int maxIterations();

	@Key(GRAD_DESC_LEARNING_RATE)
	@DefaultValue("0.01")
	public double learningRate();

	@Key(GRAD_DESC_GRADIENT_THRESHOLD)
	@DefaultValue("0.001")
	public double gradientThreshold();
}
