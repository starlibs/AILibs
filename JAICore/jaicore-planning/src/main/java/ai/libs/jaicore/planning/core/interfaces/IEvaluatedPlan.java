package ai.libs.jaicore.planning.core.interfaces;

import org.api4.java.common.attributedobjects.ScoredItem;

public interface IEvaluatedPlan<V extends Comparable<V>> extends IPlan, ScoredItem<V> {

}
