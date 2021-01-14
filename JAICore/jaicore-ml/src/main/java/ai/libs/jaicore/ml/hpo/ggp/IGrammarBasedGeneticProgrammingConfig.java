package ai.libs.jaicore.ml.hpo.ggp;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

public interface IGrammarBasedGeneticProgrammingConfig extends IOwnerBasedAlgorithmConfig {

	/**
	 * @return The size of the population.
	 */
	@Key("ggp.population_size")
	@DefaultValue("100")
	public int getPopulationSize();

	/**
	 * @return The number of best individuals to keep for the next generation.
	 */
	@Key("ggp.elitism_size")
	@DefaultValue("5")
	public int getElitismSize();

	/**
	 * @return The number of best individuals to keep for the next generation.
	 */
	@Key("ggp.tournament_size")
	@DefaultValue("2")
	public int getTournamentSize();

	/**
	 * The maximum number of generations to conduct. A value <= 0 refers to infinite number of generations and requires a timeout to be set instead.
	 * @return The maximum number of generations to conduct.
	 */
	@Key("ggp.generations")
	@DefaultValue("100")
	public int getNumGenerations();

	/**
	 * @return Maximum depth of a single tree during initialization.
	 */
	@Key("ggp.max_depth")
	@DefaultValue("50")
	public int getMaxDepth();

	/**
	 * @return The rate at which a cross over is performed.
	 */
	@Key("ggp.xover.rate")
	@DefaultValue("0.9")
	public double getCrossoverRate();

	/**
	 * @return The rate at which an individual is mutated.
	 */
	@Key("ggp.mutation.rate")
	@DefaultValue("0.1")
	public double getMutationRate();

	@Key("ggp.log.fitness_stats")
	@DefaultValue("true")
	public boolean getPrintFitnessStats();

	/**
	 * Early stopping terminates the evolutionary process early if there were no changes for a certain amount of time.
	 * If configured with a value x > 0, GGP will check whether the best solution was updated within the last x generations.
	 * As soon as the number of generations the best solution did not change exceeds x it will terminate the evolutionary run.
	 * @return The number of generations to wait for the best solution to change.
	 */
	@Key("ggp.early_stopping")
	@DefaultValue("20")
	public int getEarlyStopping();

	/**
	 * In order to increase diversity, the population (except for elite individuals) is substituted by randomly generated individuals to perform a random restart (seeded with elite individuals only).
	 * If this option is set to <= 0, this feature is deactivated.
	 * @return The number of generations after which to perform a random restart.
	 */
	@Key("ggp.random_restart")
	@DefaultValue("10")
	public int getRandomRestart();

	/**
	 * If the evaluation of an individual fails, we will need to nevertheless assign it a score. Ideally, this score is worse than
	 * any scores that can be obtained by successfully evaluating individuals.
	 * @return The score that is assigned to individuals that failed to be evaluated.
	 */
	@Key("ggp.failed_eval_score")
	@DefaultValue("10000")
	public double getFailedEvaluationScore();

}
