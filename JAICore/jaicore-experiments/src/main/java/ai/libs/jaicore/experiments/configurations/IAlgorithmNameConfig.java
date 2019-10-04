package ai.libs.jaicore.experiments.configurations;

import org.aeonbits.owner.Reloadable;
import org.aeonbits.owner.Config.Key;

import ai.libs.jaicore.basic.IOwnerBasedConfig;

public interface IAlgorithmNameConfig extends IOwnerBasedConfig, Reloadable {
	public static final String K_ALGORITHM_NAME = "algorithmname";

	@Key(K_ALGORITHM_NAME)
	public String getAlgorithmName();
}
