package jaicore.basic.algorithm;

public interface IRandomAlgorithmConfig extends IAlgorithmConfig {

	public static final String K_SEED = "seed";

	/**
	 * @return Random seed to be used for execuution
	 */
	@Key(K_SEED)
	@DefaultValue("0")
	public int seed();
}
