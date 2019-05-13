package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import ai.libs.jaicore.search.algorithms.standard.uncertainty.IUncertaintySource;

public interface IPotentiallyUncertaintyAnnotatingNodeEvaluator<N, V extends Comparable<V>> extends INodeEvaluator<N, V> {
	public void setUncertaintySource(IUncertaintySource<N,V> uncertaintySource);
	public boolean annotatesUncertainty();
}
