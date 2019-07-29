package ai.libs.jaicore.search.syntheticgraphs;

import java.util.List;

import org.api4.java.algorithm.IRandomAlgorithmConfig;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.experiments.IExperimentSetConfig;

public interface ISyntheticSearchExperimentConfig extends IExperimentSetConfig, IDatabaseConfig, IRandomAlgorithmConfig {

	public static final String KEY_BRANCHING = "branching";
	public static final String KEY_DEPTH = "depth";
	public static final String KEY_ISLANDS_DISTANCE = "islands.distance";
	public static final String KEY_ISLANDS_NUMBER_OF_TREASURES = "islands.treasures";
	public static final String KEY_FUNCTIONS = "function";
	public static final String KEY_SEARCH = "search";
	public static final String KEY_MAXITER = "maxiter";

	@Key(KEY_BRANCHING)
	public List<Integer> branchingFactors();

	@Key(KEY_DEPTH)
	public List<Integer> depths();

	@Key(KEY_ISLANDS_DISTANCE)
	public List<Double> distancesToIslands();

	@Key(KEY_ISLANDS_NUMBER_OF_TREASURES)
	public List<Integer> numbersOfTreasureIslands();

	@Key(KEY_SEARCH)
	public List<String> searchAlgorithms();

	@Key(KEY_MAXITER)
	public List<Integer> maximumIteration();

	@Key(KEY_FUNCTIONS)
	public List<String> functions();
}
