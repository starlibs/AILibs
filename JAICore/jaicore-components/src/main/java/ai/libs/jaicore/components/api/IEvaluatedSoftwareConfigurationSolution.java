package ai.libs.jaicore.components.api;

import org.api4.java.common.attributedobjects.ScoredItem;

public interface IEvaluatedSoftwareConfigurationSolution<V extends Comparable<V>> extends ScoredItem<V> {
	public IComponentInstance getComponentInstance();
}
