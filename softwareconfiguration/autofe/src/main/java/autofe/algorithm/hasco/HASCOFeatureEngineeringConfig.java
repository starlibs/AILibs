package autofe.algorithm.hasco;

import org.aeonbits.owner.Config.Sources;

import ai.libs.hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;

@Sources({ "file:conf/autofe.properties" })
public interface HASCOFeatureEngineeringConfig extends HASCOWithRandomCompletionsConfig {
	public static final String SELECTION_PORTION = "autofe.maxPipelineSize";
	public static final String K_RANDOM_SEED = "hasco.seed";
	public static final String SUBSAMPLING_RATIO = "autofe.subsamplingRatio";
	public static final String MIN_INSTANCES = "autofe.minInstances";

	/**
	 * @return The seed for the pseudo randomness generator.
	 */
	@Key(K_RANDOM_SEED)
	@DefaultValue("0")
	public int randomSeed();

	@Key(SELECTION_PORTION)
	@DefaultValue("5")
	public int maxPipelineSize();

	@Key(SUBSAMPLING_RATIO)
	@DefaultValue("0.01")
	public double subsamplingRatio();

	@Key(MIN_INSTANCES)
	@DefaultValue("200")
	public int minInstances();
}
