package ai.libs.jaicore.ml.hpo.multifidelity.hyperband;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

public interface IHyperbandConfig extends IOwnerBasedAlgorithmConfig {

	public static final String NS = "hpo.hyperband.";
	public static final String K_ETA = NS + "eta";
	public static final String K_SEED = NS + "seed";
	public static final String K_ITERATIONS = NS + "iterations";
	public static final String K_CRASH_SCORE = NS + "crash_score";

	/**
	 * The parameter eta defines that after each round eta^-1 many solutions of the current population are preserved for the next stage of a race. The default value (according to the Hyperband paper) is 3.
	 *
	 * @return The value of the parameter eta.
	 */
	@Key(K_ETA)
	@DefaultValue("3")
	public double getEta();

	/**
	 * @return The seed for the pseudo random number generator.
	 */
	@Key(K_SEED)
	@DefaultValue("42")
	public long getSeed();

	/**
	 * The number of iterations can either be defined by 'auto' to be calculated as proposed in the paper or by defining a custom positive integer. Caution: The number of candidates in the first round is exponential in the number of iterations.
	 * @return The number of iterations.
	 */
	@Key(K_ITERATIONS)
	@DefaultValue("auto")
	public String getIterations();

	/**
	 * @return The score to be assigned to crashed evaluations.
	 */
	@Key(K_CRASH_SCORE)
	@DefaultValue(Integer.MAX_VALUE + "")
	public double getCrashScore();

}
