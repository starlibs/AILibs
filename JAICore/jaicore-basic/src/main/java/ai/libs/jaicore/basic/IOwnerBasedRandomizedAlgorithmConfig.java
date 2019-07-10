package ai.libs.jaicore.basic;

import org.api4.java.algorithm.IRandomAlgorithmConfig;

public interface IOwnerBasedRandomizedAlgorithmConfig extends IOwnerBasedAlgorithmConfig, IRandomAlgorithmConfig {
	public static final String K_SEED = "seed";

	@Override
	public long seed();
}
