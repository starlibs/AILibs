package ai.libs.hasco.model;

import ai.libs.jaicore.basic.ScoredItem;

public interface EvaluatedSoftwareConfigurationSolution<V extends Comparable<V>> extends ScoredItem<V> {
	public ComponentInstance getComponentInstance();
}
