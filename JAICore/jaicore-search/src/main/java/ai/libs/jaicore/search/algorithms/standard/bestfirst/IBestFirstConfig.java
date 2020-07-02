package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.BestFirst.ParentDiscarding;

public interface IBestFirstConfig extends IOwnerBasedAlgorithmConfig {

	public static final String K_PD = "bestfirst.parentdiscarding";
	public static final String K_OE = "bestfirst.optimisticheuristic";

	/**
	 * @return Whether or not parent discarding should be used
	 */
	@Key(K_PD)
	@DefaultValue("NONE")
	public ParentDiscarding parentDiscarding();

	@Key(K_OE)
	@DefaultValue("false")
	public boolean optimisticHeuristic();
}
