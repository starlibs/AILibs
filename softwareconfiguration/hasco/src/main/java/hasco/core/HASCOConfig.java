package hasco.core;

import org.aeonbits.owner.Config.Sources;

import jaicore.basic.algorithm.IAlgorithmConfig;

@Sources({ "file:conf/hasco.properties" })
public interface HASCOConfig extends IAlgorithmConfig {
	public static final String K_VISUALIZE = "hasco.visualize";
	
	/**
	 * @return Whether or not the search conducted by HASCO should be visualized
	 */
	@Key(K_VISUALIZE)
	@DefaultValue("false")
	public boolean visualizationEnabled();
}
