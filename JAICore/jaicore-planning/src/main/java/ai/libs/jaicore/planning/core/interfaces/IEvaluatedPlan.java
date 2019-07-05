package ai.libs.jaicore.planning.core.interfaces;

import ai.libs.jaicore.basic.ScoredItem;

public interface IEvaluatedPlan<V extends Comparable<V>> extends IPlan, ScoredItem<V> {

}
