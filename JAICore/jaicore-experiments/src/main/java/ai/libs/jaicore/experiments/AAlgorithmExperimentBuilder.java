package ai.libs.jaicore.experiments;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmMaxIterConfig;
import ai.libs.jaicore.experiments.configurations.IAlgorithmNameConfig;

public abstract class AAlgorithmExperimentBuilder<B extends AAlgorithmExperimentBuilder<B>> extends AExperimentBuilder<B> {

	public AAlgorithmExperimentBuilder(final IExperimentSetConfig config) {
		super(config);
	}

	public B withAlgorithmName(final String search) {
		this.set(IAlgorithmNameConfig.K_ALGORITHM_NAME, search);
		return this.getMe();
	}

	public B withMaxiter(final int maxIter) {
		this.set(IAlgorithmMaxIterConfig.K_ALGORITHM_MAXITER, maxIter);
		return this.getMe();
	}

	public B withAlgorithmSeed(final long seed) {
		this.set(IOwnerBasedRandomConfig.K_SEED, seed);
		return this.getMe();
	}
}
