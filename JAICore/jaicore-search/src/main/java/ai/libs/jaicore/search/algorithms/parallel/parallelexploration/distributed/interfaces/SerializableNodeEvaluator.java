package ai.libs.jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.io.Serializable;

import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;

public interface SerializableNodeEvaluator<T,V extends Comparable<V>> extends INodeEvaluator<T, V>, Serializable {

}
