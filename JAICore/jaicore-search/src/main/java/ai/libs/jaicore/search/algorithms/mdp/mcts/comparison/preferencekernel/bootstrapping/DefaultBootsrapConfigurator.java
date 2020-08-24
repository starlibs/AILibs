package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison.preferencekernel.bootstrapping;

import java.util.Map;

import it.unimi.dsi.fastutil.doubles.DoubleList;

public class DefaultBootsrapConfigurator implements IBootstrapConfigurator {

	private final int factor;
	private final int minNumberOfBootstraps;
	private final int maxNumberOfBootstraps;
	private final int size;

	public DefaultBootsrapConfigurator() {
		this(2, 100, 1000, 100);
	}

	public DefaultBootsrapConfigurator(final int factor, final int minNumberOfBootstraps, final int maxNumberOfBootstraps, final int size) {
		super();
		this.factor = factor;
		this.minNumberOfBootstraps = minNumberOfBootstraps;
		this.maxNumberOfBootstraps = maxNumberOfBootstraps;
		this.size = size;
	}

	@Override
	public int getNumBootstraps(final Map<?, DoubleList> observationsPerAction) {
		return Math.min(this.maxNumberOfBootstraps, Math.max(this.minNumberOfBootstraps, this.factor * observationsPerAction.size()));
	}

	@Override
	public int getBootstrapSizePerChild(final Map<?, DoubleList> observationsPerAction) {
		return this.size;
	}
}
