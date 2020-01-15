package ai.libs.jaicore.search.syntheticgraphs.experiments;

import java.util.List;

import org.aeonbits.owner.Config.Sources;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.experiments.IExperimentSetConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmMaxIterConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmNameConfig;

@Sources({ "file:conf/synthetic-experiments.conf" })
public interface ISyntheticSearchExperimentConfig extends IExperimentSetConfig, IDatabaseConfig, IAlgorithmNameConfig, IAlgorithmMaxIterConfig, IOwnerBasedRandomConfig {

	public static final String K_BRANCHING = "branching";
	public static final String K_DEPTH = "depth";

	@Key(K_BRANCHING)
	public List<Integer> branchingFactors();

	@Key(K_DEPTH)
	public List<Integer> depths();
}
