package ai.libs.jaicore.search.syntheticgraphs.experiments;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.basic.IDatabaseConfig;
import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.experiments.IExperimentSetConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmMaxIterConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmNameConfig;

@Sources({ "file:conf/synthetic-experiments.conf" })
public interface ISyntheticSearchExperimentConfig extends IExperimentSetConfig, IDatabaseConfig, IAlgorithmNameConfig, IAlgorithmMaxIterConfig, IOwnerBasedRandomConfig {

	public static final String K_BRANCHING = "branching";
	public static final String K_DEPTH = "depth";
	public static final String K_ISLANDS_MAXISLANDSIZE = "maxislandsize";
	public static final String K_ISLANDS_NUMBER_OF_TREASURES = "treasures";
	public static final String K_TREASURE_MODEL = "treasuremodel";

	@Key(K_BRANCHING)
	public List<Integer> branchingFactors();

	@Key(K_DEPTH)
	public List<Integer> depths();

	@Key(K_ISLANDS_MAXISLANDSIZE)
	public List<Double> maxIslandSize();

	@Key(K_ISLANDS_NUMBER_OF_TREASURES)
	public List<Integer> numbersOfTreasureIslands();

	@Key(K_TREASURE_MODEL)
	public List<String> functions();
}
