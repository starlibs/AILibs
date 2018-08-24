package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import weka.core.Instances;

public interface IDataDependentNodeEvaluator<T,V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public void setData(Instances data);
}
