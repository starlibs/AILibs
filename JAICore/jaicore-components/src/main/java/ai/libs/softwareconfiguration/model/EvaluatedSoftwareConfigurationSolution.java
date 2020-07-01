package ai.libs.softwareconfiguration.model;

import org.api4.java.common.attributedobjects.ScoredItem;

public interface EvaluatedSoftwareConfigurationSolution<V extends Comparable<V>> extends ScoredItem<V> {
	public ComponentInstance getComponentInstance();
}
