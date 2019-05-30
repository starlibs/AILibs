package jaicore.ea.algorithm;

import jaicore.basic.algorithm.IAlgorithmConfig;

/**
 * General properties of an evolutionary algorithm.
 *
 * @author wever
 */
public interface IEvolutionaryAlgorithmConfig extends IAlgorithmConfig {

	public static final String K_EVALUATIONS = "evaluations";
	public static final String K_GENERATIONS = "generations";
	public static final String K_POPULATION_SIZE = "population.size";

	public static final String K_EARLY_TERMINATION_GENS = "earlytermination.generations";
	public static final String K_EARLY_TERMINATION_EPSILON = "earlytermination.epsilon";

	@Key(K_POPULATION_SIZE)
	@DefaultValue("100")
	public int populationSize();

	@Key(K_GENERATIONS)
	@DefaultValue("1000")
	public int numberOfGenerations();

	@Key(K_EVALUATIONS)
	@DefaultValue("-1")
	public int numberOfEvaluations();

	@Key(K_EARLY_TERMINATION_EPSILON)
	@DefaultValue("0.00001")
	public double earlyTerminationEpsilon();

	/**
	 * Number of generations after which early termination criterion evaluates to true.
	 * If it is set to -1, no early termination is applied.
	 * 
	 * @return Number of generations with no change after which early termination becomes true.
	 */
	@Key(K_EARLY_TERMINATION_GENS)
	@DefaultValue("-1")
	public double earlyTerminationGenerations();
}
