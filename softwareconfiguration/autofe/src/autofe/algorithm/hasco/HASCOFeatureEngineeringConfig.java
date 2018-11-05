package autofe.algorithm.hasco;

import org.aeonbits.owner.Config.Sources;

import hasco.variants.forwarddecomposition.twophase.HASCOWithRandomCompletionsConfig;
import jaicore.ml.experiments.IMultiClassClassificationExperimentConfig;

@Sources({ "file:conf/hascoimagefe.properties" })
public interface HASCOFeatureEngineeringConfig
		extends HASCOWithRandomCompletionsConfig, IMultiClassClassificationExperimentConfig {
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
