package ai.libs.jaicore.search.syntheticgraphs.experiments;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.basic.IOwnerBasedRandomConfig;

public class TreasureIslandSearchExperimentBuilder extends SyntheticSearchExperimentBuilder<TreasureIslandSearchExperimentBuilder> {

	public TreasureIslandSearchExperimentBuilder() {
		this(ConfigCache.getOrCreate(ISyntheticSearchExperimentConfig.class));
	}

	public TreasureIslandSearchExperimentBuilder(final ISyntheticSearchExperimentConfig config) {
		super(config);
	}

	public TreasureIslandSearchExperimentBuilder withSeed(final long seed) {
		this.set(IOwnerBasedRandomConfig.K_SEED, seed);
		return this;
	}

	public TreasureIslandSearchExperimentBuilder withMaxIslandSize(final double maxIslandSize) {
		this.set(ITreasureIslandExperimentSetConfig.K_ISLANDS_MAXISLANDSIZE, maxIslandSize);
		return this;
	}

	public TreasureIslandSearchExperimentBuilder withTreasures(final int numTreasures) {
		this.set(ITreasureIslandExperimentSetConfig.K_ISLANDS_NUMBER_OF_TREASURES, numTreasures);
		return this;
	}

	public TreasureIslandSearchExperimentBuilder withTreasureModel(final String treasureModel) {
		this.set(ITreasureIslandExperimentSetConfig.K_TREASURE_MODEL, treasureModel);
		return this;
	}

	@Override
	protected TreasureIslandSearchExperimentBuilder getMe() {
		return this;
	}
}
