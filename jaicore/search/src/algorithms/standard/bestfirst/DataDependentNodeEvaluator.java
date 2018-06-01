package jaicore.search.algorithms.standard.bestfirst;

import jaicore.search.algorithms.standard.core.INodeEvaluator;
import weka.core.Instances;

public interface DataDependentNodeEvaluator<T,V extends Comparable<V>> extends INodeEvaluator<T, V> {
	public void setData(Instances data);
}
