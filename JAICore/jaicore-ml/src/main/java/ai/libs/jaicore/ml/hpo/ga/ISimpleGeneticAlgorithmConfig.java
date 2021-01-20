package ai.libs.jaicore.ml.hpo.ga;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

public interface ISimpleGeneticAlgorithmConfig extends IOwnerBasedAlgorithmConfig {

	public static final String K_SEED = "jaicore.ml.hpo.ga.seed";
	public static final String K_FAILED_FITNESS = "jaicore.ml.hpo.ga.failed_eval_objective_value";
	public static final String K_ELITE_SIZE = "jaicore.ml.hpo.ga.elite_size";
	public static final String K_MUTATION_RATE = "jaicore.ml.hpo.ga.mutation_rate";
	public static final String K_CROSSOVER_RATE = "jaicore.ml.hpo.ga.crossover_rate";
	public static final String K_POPULATION_SIZE = "jaicore.ml.hpo.ga.population_size";
	public static final String K_EARLY_TERMINATION = "jaicore.ml.hpo.ga.early_termination";
	public static final String K_SOFT_RESTART = "jaicore.ml.hpo.ga.soft_restart";

	public static final String K_MAX_EVALUATIONS = "jaicore.ml.hpo.ga.max_evaluations";
	public static final String K_MAX_GENERATIONS = "jaicore.ml.hpo.ga.max_generations";
	public static final String K_MAX_RUNTIME = "jaicore.ml.hpo.ga.max_runtime_ms";

	public static final String K_LOG_GENERATION_STATS = "jaicore.ml.hpo.ga.out.generation_stats";

	/**
	 *
	 * @return
	 */
	@Key(K_MAX_EVALUATIONS)
	@DefaultValue("-1")
	public int getMaxEvaluations();

	@Key(K_MAX_GENERATIONS)
	@DefaultValue("100")
	public int getMaxGenerations();

	@Key(K_MAX_RUNTIME)
	@DefaultValue("-1")
	public long getMaxRuntimeInMS();

	@Key(K_SEED)
	@DefaultValue("0")
	public long getSeed();

	@Key(K_MUTATION_RATE)
	@DefaultValue("0.1")
	public double getMutationRate();

	@Key(K_CROSSOVER_RATE)
	@DefaultValue("0.9")
	public double getCrossoverRate();

	@Key(K_POPULATION_SIZE)
	@DefaultValue("100")
	public int getPopulationSize();

	@Key(K_EARLY_TERMINATION)
	@DefaultValue("0")
	public int getEarlyTermination();

	@Key(K_FAILED_FITNESS)
	@DefaultValue("2.0")
	public double getFailedEvalObjectiveValue();

	@Key(K_SOFT_RESTART)
	@DefaultValue("10")
	public int getSoftRestart();

	@Key(K_ELITE_SIZE)
	@DefaultValue("1")
	public int getEliteSize();

	@Key(K_LOG_GENERATION_STATS)
	@DefaultValue("true")
	public boolean getPrintGenerationStats();

}
