package ai.libs.jaicore.basic;

import org.api4.java.algorithm.IRandomAlgorithmConfig;

/**
 * Random configurations can be used to provide a random seed.
 * Random can occur in both an underlying problem instance, an algorithm used to tackle the problem, or both
 *
 * @author fmohr
 *
 */
public interface IOwnerBasedRandomConfig extends IRandomAlgorithmConfig {
	public static final String K_SEED = "seed";

	@Override
	public long seed();
}
