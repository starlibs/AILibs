package hasco.model;

import jaicore.basic.ScoredItem;

public interface EvaluatedSoftwareConfigurationSolution<V extends Comparable<V>> extends ScoredItem<V> {
	public ComponentInstance getComponentInstance();
}
