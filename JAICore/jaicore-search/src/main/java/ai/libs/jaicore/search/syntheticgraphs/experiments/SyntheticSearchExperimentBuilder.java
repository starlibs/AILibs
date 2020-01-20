package ai.libs.jaicore.search.syntheticgraphs.experiments;

import ai.libs.jaicore.experiments.AAlgorithmExperimentBuilder;

public abstract class SyntheticSearchExperimentBuilder<B extends SyntheticSearchExperimentBuilder<B>> extends AAlgorithmExperimentBuilder<B> {

	public SyntheticSearchExperimentBuilder(final ISyntheticSearchExperimentConfig config) {
		super(config);
	}

	public B withBF(final int branchingFactor) {
		this.set(ISyntheticSearchExperimentConfig.K_BRANCHING, branchingFactor);
		return this.getMe();
	}

	public B withDepth(final int depth) {
		this.set(ISyntheticSearchExperimentConfig.K_DEPTH, depth);
		return this.getMe();
	}
}
