package ai.libs.jaicore.ml.ranking.label.learner.rpc;

import java.util.Map;

import org.aeonbits.owner.Config;

public interface IRPCConfig extends Config, Map<String, Config> {

	public static final String K_BASE_LEARNER = "rpc.baselearner";
	public static final String K_VOTING_STRATEGY = "rpc.votingstrategy";

	public static final String V_VOTING_STRATEGY_CLASSIFY = "classify";
	public static final String V_VOTING_STRATEGY_PROBABILITY = "probability";

	@Key(K_BASE_LEARNER)
	@DefaultValue("weka.classifiers.trees.RandomForest")
	public String getBaseLearner();

	@Key(K_VOTING_STRATEGY)
	@DefaultValue(V_VOTING_STRATEGY_PROBABILITY)
	public String getVotingStrategy();

}
