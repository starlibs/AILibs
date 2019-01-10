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

	@Key(K_POPULATION_SIZE)
	@DefaultValue("100")
	public int populationSize();

	@Key(K_GENERATIONS)
	@DefaultValue("1000")
	public int numberOfGenerations();

	@Key(K_EVALUATIONS)
	@DefaultValue("-1")
	public int numberOfEvaluations();
}
