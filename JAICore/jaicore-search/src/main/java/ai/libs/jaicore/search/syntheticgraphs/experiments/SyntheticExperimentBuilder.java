package ai.libs.jaicore.search.syntheticgraphs.experiments;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;
import ai.libs.jaicore.experiments.AAlgorithmExperimentBuilder;

public class SyntheticExperimentBuilder extends AAlgorithmExperimentBuilder<SyntheticExperimentBuilder> {

	public SyntheticExperimentBuilder() {
		this(ConfigCache.getOrCreate(ISyntheticSearchExperimentConfig.class));
	}

	public SyntheticExperimentBuilder(final ISyntheticSearchExperimentConfig config) {
		super(config);
	}

	public SyntheticExperimentBuilder withSeed(final long seed) {
		this.set(IOwnerBasedRandomConfig.K_SEED, seed);
		return this;
	}

	public SyntheticExperimentBuilder withBF(final int branchingFactor) {
		this.set(ISyntheticSearchExperimentConfig.K_BRANCHING, branchingFactor);
		return this;
	}

	public SyntheticExperimentBuilder withDepth(final int depth) {
		this.set(ISyntheticSearchExperimentConfig.K_DEPTH, depth);
		return this;
	}

	public SyntheticExperimentBuilder withMaxIslandSize(final double maxIslandSize) {
		this.set(ISyntheticSearchExperimentConfig.K_ISLANDS_MAXISLANDSIZE, maxIslandSize);
		return this;
	}

	public SyntheticExperimentBuilder withTreasures(final int numTreasures) {
		this.set(ISyntheticSearchExperimentConfig.K_ISLANDS_NUMBER_OF_TREASURES, numTreasures);
		return this;
	}

	public SyntheticExperimentBuilder withTreasureModel(final String treasureModel) {
		this.set(ISyntheticSearchExperimentConfig.K_TREASURE_MODEL, treasureModel);
		return this;
	}

	@Override
	protected SyntheticExperimentBuilder getMe() {
		return this;
	}
}
