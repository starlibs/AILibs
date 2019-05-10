package jaicore.search.algorithms.standard.bestfirst;

import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.search.algorithms.standard.bestfirst.BestFirst.ParentDiscarding;

public interface IBestFirstConfig extends IAlgorithmConfig {
	
	public static final String K_PD = "bestfirst.parentdiscarding";

	/**
	 * @return Whether or not parent discarding should be used
	 */
	@Key(K_PD)
	@DefaultValue("NONE")
	public ParentDiscarding parentDiscarding();
}
