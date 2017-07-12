package jaicore.search.algorithms.parallel.parallelexploration.distributed.interfaces;

import java.io.Serializable;

import jaicore.search.algorithms.standard.core.NodeEvaluator;

public interface SerializableNodeEvaluator<T,V extends Comparable<V>> extends NodeEvaluator<T, V>, Serializable {

}
