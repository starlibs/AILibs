package ai.libs.jaicore.ml.hpo.ga;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

public interface ISimpleGeneticAlgorithmConfig extends IOwnerBasedAlgorithmConfig {

	/**
	 *
	 * @return
	 */
	@Key("jaicore.ml.hpo.ga.max_evaluations")
	@DefaultValue("-1")
	public int getMaxEvaluations();

	@Key("jaicore.ml.hpo.ga.max_generations")
	@DefaultValue("100")
	public int getMaxGenerations();

	@Key("jaicore.ml.hpo.ga.max_runtime_ms")
	@DefaultValue("-1")
	public long getMaxRuntimeInMS();

	@Key("jaicore.ml.hpo.ga.seed")
	@DefaultValue("0")
	public long getSeed();

	@Key("jaicore.ml.hpo.ga.mutation_rate")
	@DefaultValue("0.1")
	public double getMutationRate();

	@Key("jaicore.ml.hpo.ga.crossover_rate")
	@DefaultValue("0.9")
	public double getCrossoverRate();

	@Key("jaicore.ml.hpo.ga.population_size")
	@DefaultValue("100")
	public int getPopulationSize();

	@Key("jaicore.ml.hpo.ga.early_termination")
	@DefaultValue("0")
	public int getEarlyTermination();

	@Key("jaicore.ml.hpo.ga.failed_eval_objective_value")
	@DefaultValue("2.0")
	public double getFailedEvalObjectiveValue();

	@Key("jaicore.ml.hpo.ga.soft_restart")
	@DefaultValue("10")
	public int getSoftRestart();

	@Key("jaicore.ml.hpo.ga.elite_size")
	@DefaultValue("1")
	public int getEliteSize();

}
