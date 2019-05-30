package jaicore.ea.algorithm.moea.moeaframework;

import jaicore.ea.algorithm.IEvolutionaryAlgorithmConfig;
import jaicore.ea.algorithm.moea.moeaframework.util.EMOEAFrameworkAlgorithmName;

public interface IMOEAFrameworkAlgorithmConfig extends IEvolutionaryAlgorithmConfig {

	public static final String K_MOEAFRAMEWORK_ALGORITHM_NAME = "moeaframework.algorithm";
	public static final String K_CROSSOVER_RATE = "moeaframework.sbx.rate";
	public static final String K_CROSSOVER_DIST_INDEX = "moeaframework.sbx.distributionIndex";
	public static final String K_PERMUTATION_RATE = "moeaframework.pm.rate";
	public static final String K_PERMUTATION_DIST_INDEX = "moeaframework.pm.distributionIndex";
	public static final String K_WITH_REPLACEMENT = "moeaframework.withReplacement";

	@Key(K_MOEAFRAMEWORK_ALGORITHM_NAME)
	@DefaultValue("NSGAII")
	public EMOEAFrameworkAlgorithmName algorithmName();

	@Key(K_CROSSOVER_RATE)
	@DefaultValue("0.9")
	public double crossoverRate();

	@Key(K_CROSSOVER_DIST_INDEX)
	@DefaultValue("10")
	public double crossoverDistIndex();

	@Key(K_PERMUTATION_RATE)
	@DefaultValue("0.1")
	public double mutationRate();

	@Key(K_PERMUTATION_DIST_INDEX)
	@DefaultValue("10")
	public double mutationDistIndex();

	@Key(K_WITH_REPLACEMENT)
	@DefaultValue("false")
	public double withReplacement();

}
