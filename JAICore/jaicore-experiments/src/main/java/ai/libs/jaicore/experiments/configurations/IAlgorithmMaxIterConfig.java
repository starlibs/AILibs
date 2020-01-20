package ai.libs.jaicore.experiments.configurations;

import org.aeonbits.owner.Reloadable;

import ai.libs.jaicore.basic.IOwnerBasedConfig;

public interface IAlgorithmMaxIterConfig extends IOwnerBasedConfig, Reloadable {
	public static final String K_ALGORITHM_MAXITER = "maxiter";

	@Key(K_ALGORITHM_MAXITER)
	public String getMaxIterations();
}
