package ai.libs.jaicore.ml.hpo.multifidelity.hyperband;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

public interface IHyperbandConfig extends IOwnerBasedAlgorithmConfig {

	public static final String NS = "hpo.hyperband.";
	public static final String K_ETA = NS + "eta";
	public static final String K_SEED = NS + "seed";
	public static final String K_ITERATIONS = NS + "iterations";
	public static final String K_CRASH_SCORE = NS + "crash_score";

	@Key(K_ETA)
	@DefaultValue("3")
	public double getEta();

	@Key(K_SEED)
	@DefaultValue("42")
	public long getSeed();

	@Key(K_ITERATIONS)
	@DefaultValue("auto")
	public String getIterations();

	@Key(K_CRASH_SCORE)
	@DefaultValue(Integer.MAX_VALUE + "")
	public double getCrashScore();

}
