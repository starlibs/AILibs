package ai.libs.jaicore.ml.hpo.ga;

import ai.libs.jaicore.components.api.IComponentInstance;
import ai.libs.jaicore.components.api.IEvaluatedSoftwareConfigurationSolution;

public class EvaluatedGASolution<V extends Comparable<V>> implements IEvaluatedSoftwareConfigurationSolution<V> {

	private final IComponentInstance ci;
	private final V score;

	public EvaluatedGASolution(final IComponentInstance ci, final V score) {
		this.ci = ci;
		this.score = score;
	}

	@Override
	public V getScore() {
		return this.score;
	}

	@Override
	public IComponentInstance getComponentInstance() {
		return this.ci;
	}

}
