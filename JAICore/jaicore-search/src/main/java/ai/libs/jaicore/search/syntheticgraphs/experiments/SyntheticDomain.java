package ai.libs.jaicore.search.syntheticgraphs.experiments;

import org.aeonbits.owner.ConfigCache;

import ai.libs.jaicore.search.experiments.SearchExperimentDomain;
import ai.libs.jaicore.search.syntheticgraphs.ISyntheticTreasureIslandProblem;
import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class SyntheticDomain extends SearchExperimentDomain<TreasureIslandSearchExperimentBuilder, ISyntheticTreasureIslandProblem, ITransparentTreeNode, Integer> {

	public SyntheticDomain() {
		this(ConfigCache.getOrCreate(ISyntheticSearchExperimentConfig.class));
	}

	public SyntheticDomain(final ISyntheticSearchExperimentConfig config) {
		super(config, new SyntheticExperimentDecoder(config));
	}

	@Override
	public Class<TreasureIslandSearchExperimentBuilder> getBuilderClass() {
		return TreasureIslandSearchExperimentBuilder.class;
	}

	@Override
	public SyntheticExperimentDecoder getDecoder() {
		return (SyntheticExperimentDecoder)super.getDecoder();
	}
}
